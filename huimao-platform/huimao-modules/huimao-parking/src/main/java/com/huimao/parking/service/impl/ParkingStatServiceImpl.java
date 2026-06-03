package com.huimao.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huimao.common.core.text.Convert;
import com.huimao.common.core.utils.ServletUtils;
import com.huimao.common.mybatisplus.constant.MybatisPageConstants;
import com.huimao.parking.constant.ParkingConstants;
import com.huimao.parking.domain.ParkingDailyStat;
import com.huimao.parking.domain.ParkingGate;
import com.huimao.parking.domain.ParkingLot;
import com.huimao.parking.domain.ParkingOrder;
import com.huimao.parking.domain.ParkingPayment;
import com.huimao.parking.domain.ParkingSession;
import com.huimao.parking.mapper.ParkingDailyStatMapper;
import com.huimao.parking.mapper.ParkingGateMapper;
import com.huimao.parking.mapper.ParkingLotMapper;
import com.huimao.parking.mapper.ParkingOrderMapper;
import com.huimao.parking.mapper.ParkingPaymentMapper;
import com.huimao.parking.mapper.ParkingSessionMapper;
import com.huimao.parking.service.IParkingStatService;
import com.huimao.parking.vo.ParkingDashboardVo;
import com.huimao.parking.vo.ParkingStatOverviewVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 收入统计服务。
 */
@Service
public class ParkingStatServiceImpl extends AbstractParkingTenantService implements IParkingStatService
{
    @Autowired
    private ParkingDailyStatMapper parkingDailyStatMapper;

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    @Autowired
    private ParkingGateMapper parkingGateMapper;

    @Autowired
    private ParkingSessionMapper parkingSessionMapper;

    @Autowired
    private ParkingOrderMapper parkingOrderMapper;

    @Autowired
    private ParkingPaymentMapper parkingPaymentMapper;

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<ParkingDailyStat> selectDailyPage(ParkingDailyStat query)
    {
        LambdaQueryWrapper<ParkingDailyStat> wrapper = buildWrapper(query);
        wrapper.orderByDesc(ParkingDailyStat::getStatDate).orderByDesc(ParkingDailyStat::getStatId);
        List<ParkingDailyStat> records = parkingDailyStatMapper.selectList(wrapper);
        List<ParkingDailyStat> summaryRecords = mergeDailyStatsByLot(records, query);
        enrichLotName(summaryRecords);
        return pageList(summaryRecords);
    }

    @Override
    public ParkingStatOverviewVo selectOverview(ParkingDailyStat query)
    {
        List<ParkingDailyStat> records = parkingDailyStatMapper.selectList(buildWrapper(query));
        int totalEntry = records.stream().map(ParkingDailyStat::getEntryCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        int totalExit = records.stream().map(ParkingDailyStat::getExitCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        Date beginDate = queryBeginDate(query, startOfDay(new Date()));
        Date endDate = queryEndDate(query, startOfDay(new Date()));
        BigDecimal totalIncome = sumPaidAmount(resolveQueryTenantId(query.getTenantId()), query.getLotId(), beginDate, endDate);
        int totalPaidOrders = countPaidPayment(resolveQueryTenantId(query.getTenantId()), query.getLotId(), beginDate, endDate);

        ParkingStatOverviewVo vo = new ParkingStatOverviewVo();
        vo.setTotalIncome(totalIncome);
        vo.setTotalEntry(totalEntry);
        vo.setTotalExit(totalExit);
        vo.setAvgOrder(totalPaidOrders == 0 ? BigDecimal.ZERO : totalIncome.divide(BigDecimal.valueOf(totalPaidOrders), 2, RoundingMode.HALF_UP));
        return vo;
    }

    @Override
    public int refreshDailyStat(ParkingDailyStat query)
    {
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        Date beginDate = statBeginDate(query, startOfDay(new Date()));
        Date endDate = statEndDate(query, startOfDay(new Date()));
        List<ParkingLot> lots = selectLots(tenantId);
        if (query.getLotId() != null)
        {
            lots = lots.stream().filter(item -> query.getLotId().equals(item.getLotId())).collect(Collectors.toList());
        }
        int count = 0;
        for (LocalDate day = toLocalDate(beginDate); !day.isAfter(toLocalDate(endDate)); day = day.plusDays(1))
        {
            Date dayStart = toDate(day);
            Date nextDayStart = toDate(day.plusDays(1));
            for (ParkingLot lot : lots)
            {
                upsertDailyStat(lot, dayStart, nextDayStart);
                count++;
            }
        }
        return count;
    }

    @Override
    public ParkingDashboardVo selectDashboard(Long lotId, String startTime, String endTime)
    {
        Long tenantId = resolveQueryTenantId(null);
        List<ParkingLot> lots = selectLots(tenantId);
        ParkingDashboardVo vo = new ParkingDashboardVo();
        vo.setLotOptions(lots.stream().map(this::toLotOption).collect(Collectors.toList()));
        ParkingLot currentLot = findCurrentLot(lots, lotId);
        vo.setLotInfo(buildLotInfo(currentLot, tenantId));

        Date todayStart = startOfDay(new Date());
        Date tomorrowStart = addDays(todayStart, 1);
        Date yesterdayStart = addDays(todayStart, -1);
        vo.setToday(buildTodayStats(tenantId, currentLot, todayStart, tomorrowStart, yesterdayStart));
        vo.setMonth(buildMonthStats(tenantId, currentLot));
        vo.setTrend(buildTrendStats(tenantId, currentLot, parseDate(startTime, addDays(todayStart, -7)), parseDate(endTime, endOfDay(new Date()))));
        return vo;
    }

    private LambdaQueryWrapper<ParkingDailyStat> buildWrapper(ParkingDailyStat query)
    {
        LambdaQueryWrapper<ParkingDailyStat> wrapper = Wrappers.lambdaQuery();
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            wrapper.eq(ParkingDailyStat::getTenantId, tenantId);
        }
        if (query.getLotId() != null)
        {
            wrapper.eq(ParkingDailyStat::getLotId, query.getLotId());
        }
        Object beginDate = query.getParams().get("beginStatDate");
        Object endDate = query.getParams().get("endStatDate");
        if (beginDate != null)
        {
            wrapper.ge(ParkingDailyStat::getStatDate, beginDate);
        }
        if (endDate != null)
        {
            wrapper.le(ParkingDailyStat::getStatDate, endDate);
        }
        wrapper.and(item -> item.gt(ParkingDailyStat::getEntryCount, 0)
                .or().gt(ParkingDailyStat::getExitCount, 0)
                .or().gt(ParkingDailyStat::getInParkCount, 0)
                .or().gt(ParkingDailyStat::getOrderCount, 0)
                .or().gt(ParkingDailyStat::getPaidOrderCount, 0)
                .or().gt(ParkingDailyStat::getIncomeAmount, BigDecimal.ZERO));
        return wrapper;
    }

    private void upsertDailyStat(ParkingLot lot, Date dayStart, Date nextDayStart)
    {
        ParkingDailyStat stat = new ParkingDailyStat();
        stat.setTenantId(lot.getTenantId());
        stat.setLotId(lot.getLotId());
        stat.setStatDate(dayStart);
        stat.setEntryCount(countEntry(lot.getTenantId(), lot, dayStart, nextDayStart));
        stat.setExitCount(countExit(lot.getTenantId(), lot, dayStart, nextDayStart));
        stat.setInParkCount(countInParkAt(lot.getTenantId(), lot, nextDayStart));
        stat.setOrderCount(countOrder(lot.getTenantId(), lot, dayStart, nextDayStart, false));
        stat.setPaidOrderCount(countPaidPayment(lot.getTenantId(), lot, dayStart, nextDayStart));
        stat.setIncomeAmount(sumPaidAmount(lot.getTenantId(), lot, dayStart, nextDayStart));
        stat.setAvgOrderAmount(stat.getPaidOrderCount() == null || stat.getPaidOrderCount() == 0
                ? BigDecimal.ZERO
                : stat.getIncomeAmount().divide(BigDecimal.valueOf(stat.getPaidOrderCount()), 2, RoundingMode.HALF_UP));
        Integer totalSpace = defaultInt(lot.getTotalSpace());
        stat.setTurnoverRate(totalSpace == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(stat.getExitCount()).divide(BigDecimal.valueOf(totalSpace), 2, RoundingMode.HALF_UP));
        stat.setCreateTime(new Date());
        stat.setUpdateTime(new Date());

        ParkingDailyStat exist = parkingDailyStatMapper.selectOne(Wrappers.<ParkingDailyStat>lambdaQuery()
                .eq(ParkingDailyStat::getTenantId, lot.getTenantId())
                .eq(ParkingDailyStat::getLotId, lot.getLotId())
                .eq(ParkingDailyStat::getStatDate, dayStart));
        if (exist == null)
        {
            parkingDailyStatMapper.insert(stat);
            return;
        }
        stat.setStatId(exist.getStatId());
        stat.setCreateTime(exist.getCreateTime());
        parkingDailyStatMapper.updateById(stat);
    }

    private List<ParkingLot> selectLots(Long tenantId)
    {
        LambdaQueryWrapper<ParkingLot> wrapper = Wrappers.lambdaQuery();
        if (tenantId != null)
        {
            wrapper.eq(ParkingLot::getTenantId, tenantId);
        }
        wrapper.eq(ParkingLot::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .orderByAsc(ParkingLot::getLotId);
        return parkingLotMapper.selectList(wrapper);
    }

    private ParkingDashboardVo.LotOption toLotOption(ParkingLot lot)
    {
        ParkingDashboardVo.LotOption option = new ParkingDashboardVo.LotOption();
        option.setLotId(lot.getLotId());
        option.setLotName(lot.getLotName());
        return option;
    }

    private ParkingLot findCurrentLot(List<ParkingLot> lots, Long lotId)
    {
        if (lotId == null)
        {
            return null;
        }
        return lots.stream().filter(item -> lotId.equals(item.getLotId())).findFirst().orElse(null);
    }

    private ParkingDashboardVo.LotInfo buildLotInfo(ParkingLot lot, Long tenantId)
    {
        ParkingDashboardVo.LotInfo info = new ParkingDashboardVo.LotInfo();
        if (lot != null)
        {
            info.setLotId(lot.getLotId());
            info.setLotName(lot.getLotName());
            info.setTotalSpace(defaultInt(lot.getTotalSpace()));
        }
        else
        {
            info.setLotName("全部停车场");
            info.setTotalSpace(sumTotalSpace(tenantId));
        }
        info.setEntryGateCount(countGate(tenantId, lot, ParkingConstants.GATE_TYPE_ENTRY));
        info.setExitGateCount(countGate(tenantId, lot, ParkingConstants.GATE_TYPE_EXIT));
        return info;
    }

    private Integer sumTotalSpace(Long tenantId)
    {
        return selectLots(tenantId).stream().map(ParkingLot::getTotalSpace).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

    private Integer countGate(Long tenantId, ParkingLot lot, String gateType)
    {
        LambdaQueryWrapper<ParkingGate> wrapper = Wrappers.lambdaQuery();
        if (tenantId != null)
        {
            wrapper.eq(ParkingGate::getTenantId, tenantId);
        }
        if (lot != null)
        {
            wrapper.eq(ParkingGate::getLotId, lot.getLotId());
        }
        wrapper.eq(ParkingGate::getGateType, gateType)
                .eq(ParkingGate::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL);
        return parkingGateMapper.selectCount(wrapper).intValue();
    }

    private ParkingDashboardVo.TodayStats buildTodayStats(Long tenantId, ParkingLot lot, Date todayStart, Date tomorrowStart, Date yesterdayStart)
    {
        ParkingDashboardVo.TodayStats stats = new ParkingDashboardVo.TodayStats();
        int todayEntry = countEntry(tenantId, lot, todayStart, tomorrowStart);
        int todayExit = countExit(tenantId, lot, todayStart, tomorrowStart);
        int yesterdayEntry = countEntry(tenantId, lot, yesterdayStart, todayStart);
        int yesterdayExit = countExit(tenantId, lot, yesterdayStart, todayStart);
        BigDecimal todayIncome = sumPaidAmount(tenantId, lot, todayStart, tomorrowStart);
        BigDecimal yesterdayIncome = sumPaidAmount(tenantId, lot, yesterdayStart, todayStart);
        int inParkCount = countInPark(tenantId, lot);
        int totalSpace = lot != null ? defaultInt(lot.getTotalSpace()) : sumTotalSpace(tenantId);

        stats.setEntryCount(todayEntry);
        stats.setExitCount(todayExit);
        stats.setInParkCount(inParkCount);
        stats.setIncomeAmount(todayIncome);
        stats.setEntryCompare(calcCompare(BigDecimal.valueOf(todayEntry), BigDecimal.valueOf(yesterdayEntry)));
        stats.setExitCompare(calcCompare(BigDecimal.valueOf(todayExit), BigDecimal.valueOf(yesterdayExit)));
        stats.setIncomeCompare(calcCompare(todayIncome, yesterdayIncome));
        stats.setSpaceUseRate(totalSpace == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(inParkCount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalSpace), 2, RoundingMode.HALF_UP));
        return stats;
    }

    private ParkingDashboardVo.MonthStats buildMonthStats(Long tenantId, ParkingLot lot)
    {
        ParkingDashboardVo.MonthStats stats = new ParkingDashboardVo.MonthStats();
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        List<ParkingDailyStat> currentMonthRecords = selectDailyStats(tenantId, lot, toDate(monthStart), toDate(nextMonthStart.minusDays(1)));
        List<ParkingDailyStat> lastMonthRecords = selectDailyStats(tenantId, lot, toDate(lastMonthStart), toDate(monthStart.minusDays(1)));

        BigDecimal income = sumIncome(currentMonthRecords);
        int orders = sumOrders(currentMonthRecords);
        BigDecimal avgOrder = orders == 0 ? BigDecimal.ZERO : income.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
        BigDecimal turnover = avg(currentMonthRecords, ParkingDailyStat::getTurnoverRate);
        BigDecimal lastIncome = sumIncome(lastMonthRecords);
        int lastOrders = sumOrders(lastMonthRecords);
        BigDecimal lastAvgOrder = lastOrders == 0 ? BigDecimal.ZERO : lastIncome.divide(BigDecimal.valueOf(lastOrders), 2, RoundingMode.HALF_UP);
        BigDecimal lastTurnover = avg(lastMonthRecords, ParkingDailyStat::getTurnoverRate);

        stats.setIncomeAmount(income);
        stats.setOrderCount(orders);
        stats.setAvgOrderAmount(avgOrder);
        stats.setTurnoverRate(turnover);
        stats.setIncomeCompare(calcCompare(income, lastIncome));
        stats.setOrderCompare(calcCompare(BigDecimal.valueOf(orders), BigDecimal.valueOf(lastOrders)));
        stats.setAvgOrderCompare(calcCompare(avgOrder, lastAvgOrder));
        stats.setTurnoverCompare(calcCompare(turnover, lastTurnover));

        for (int i = 5; i >= 0; i--)
        {
            LocalDate start = monthStart.minusMonths(i);
            LocalDate end = start.plusMonths(1).minusDays(1);
            List<ParkingDailyStat> records = selectDailyStats(tenantId, lot, toDate(start), toDate(end));
            BigDecimal monthIncome = sumIncome(records);
            int monthOrders = sumOrders(records);
            stats.getXData().add(start.format(MONTH_FORMATTER));
            stats.getIncomeData().add(monthIncome);
            stats.getOrderData().add(monthOrders);
            stats.getAvgOrderData().add(monthOrders == 0 ? BigDecimal.ZERO : monthIncome.divide(BigDecimal.valueOf(monthOrders), 2, RoundingMode.HALF_UP));
            stats.getTurnoverData().add(avg(records, ParkingDailyStat::getTurnoverRate));
        }
        return stats;
    }

    private ParkingDashboardVo.TrendStats buildTrendStats(Long tenantId, ParkingLot lot, Date startTime, Date endTime)
    {
        ParkingDashboardVo.TrendStats stats = new ParkingDashboardVo.TrendStats();
        LocalDate start = toLocalDate(startTime);
        LocalDate end = toLocalDate(endTime);
        List<ParkingDailyStat> records = selectDailyStats(tenantId, lot, startTime, endTime);
        Map<LocalDate, BigDecimal> incomeMap = records.stream().collect(Collectors.groupingBy(item -> toLocalDate(item.getStatDate()),
                Collectors.mapping(item -> defaultAmount(item.getIncomeAmount()), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1))
        {
            stats.getDate().add(day.format(DAY_FORMATTER));
            stats.getMoney().add(incomeMap.getOrDefault(day, BigDecimal.ZERO));
        }
        return stats;
    }

    private List<ParkingDailyStat> selectDailyStats(Long tenantId, ParkingLot lot, Date beginDate, Date endDate)
    {
        LambdaQueryWrapper<ParkingDailyStat> wrapper = Wrappers.lambdaQuery();
        if (tenantId != null)
        {
            wrapper.eq(ParkingDailyStat::getTenantId, tenantId);
        }
        if (lot != null)
        {
            wrapper.eq(ParkingDailyStat::getLotId, lot.getLotId());
        }
        wrapper.ge(ParkingDailyStat::getStatDate, beginDate)
                .le(ParkingDailyStat::getStatDate, endDate)
                .orderByAsc(ParkingDailyStat::getStatDate);
        return parkingDailyStatMapper.selectList(wrapper);
    }

    private int countEntry(Long tenantId, ParkingLot lot, Date beginTime, Date endTime)
    {
        LambdaQueryWrapper<ParkingSession> wrapper = Wrappers.lambdaQuery();
        applySessionFilter(wrapper, tenantId, lot);
        wrapper.ge(ParkingSession::getEntryTime, beginTime).lt(ParkingSession::getEntryTime, endTime);
        return parkingSessionMapper.selectCount(wrapper).intValue();
    }

    private int countExit(Long tenantId, ParkingLot lot, Date beginTime, Date endTime)
    {
        LambdaQueryWrapper<ParkingSession> wrapper = Wrappers.lambdaQuery();
        applySessionFilter(wrapper, tenantId, lot);
        wrapper.ge(ParkingSession::getExitTime, beginTime).lt(ParkingSession::getExitTime, endTime);
        return parkingSessionMapper.selectCount(wrapper).intValue();
    }

    private int countInPark(Long tenantId, ParkingLot lot)
    {
        LambdaQueryWrapper<ParkingSession> wrapper = Wrappers.lambdaQuery();
        applySessionFilter(wrapper, tenantId, lot);
        wrapper.ne(ParkingSession::getStatus, ParkingConstants.SESSION_STATUS_COMPLETED);
        return parkingSessionMapper.selectCount(wrapper).intValue();
    }

    private int countInParkAt(Long tenantId, ParkingLot lot, Date time)
    {
        LambdaQueryWrapper<ParkingSession> wrapper = Wrappers.lambdaQuery();
        applySessionFilter(wrapper, tenantId, lot);
        wrapper.lt(ParkingSession::getEntryTime, time)
                .and(item -> item.isNull(ParkingSession::getExitTime).or().ge(ParkingSession::getExitTime, time));
        return parkingSessionMapper.selectCount(wrapper).intValue();
    }

    private int countOrder(Long tenantId, ParkingLot lot, Date beginTime, Date endTime, boolean paidOnly)
    {
        LambdaQueryWrapper<ParkingOrder> wrapper = Wrappers.lambdaQuery();
        if (tenantId != null)
        {
            wrapper.eq(ParkingOrder::getTenantId, tenantId);
        }
        if (lot != null)
        {
            wrapper.eq(ParkingOrder::getLotId, lot.getLotId());
        }
        wrapper.ge(ParkingOrder::getCreateTime, beginTime).lt(ParkingOrder::getCreateTime, endTime);
        if (paidOnly)
        {
            wrapper.eq(ParkingOrder::getPayStatus, ParkingConstants.PAY_STATUS_SUCCESS);
        }
        return parkingOrderMapper.selectCount(wrapper).intValue();
    }

    private void applySessionFilter(LambdaQueryWrapper<ParkingSession> wrapper, Long tenantId, ParkingLot lot)
    {
        if (tenantId != null)
        {
            wrapper.eq(ParkingSession::getTenantId, tenantId);
        }
        if (lot != null)
        {
            wrapper.eq(ParkingSession::getLotId, lot.getLotId());
        }
    }

    private BigDecimal sumPaidAmount(Long tenantId, ParkingLot lot, Date beginTime, Date endTime)
    {
        return sumPaidAmount(tenantId, lot == null ? null : lot.getLotId(), beginTime, endTime);
    }

    private BigDecimal sumPaidAmount(Long tenantId, Long lotId, Date beginTime, Date endTime)
    {
        LambdaQueryWrapper<ParkingPayment> wrapper = Wrappers.lambdaQuery();
        if (tenantId != null)
        {
            wrapper.eq(ParkingPayment::getTenantId, tenantId);
        }
        if (lotId != null)
        {
            wrapper.eq(ParkingPayment::getLotId, lotId);
        }
        wrapper.eq(ParkingPayment::getPayStatus, ParkingConstants.PAY_STATUS_SUCCESS)
                .ge(ParkingPayment::getPaySuccessTime, beginTime)
                .lt(ParkingPayment::getPaySuccessTime, endTime);
        return parkingPaymentMapper.selectList(wrapper).stream().map(ParkingPayment::getPayAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int countPaidPayment(Long tenantId, ParkingLot lot, Date beginTime, Date endTime)
    {
        return countPaidPayment(tenantId, lot == null ? null : lot.getLotId(), beginTime, endTime);
    }

    private int countPaidPayment(Long tenantId, Long lotId, Date beginTime, Date endTime)
    {
        LambdaQueryWrapper<ParkingPayment> wrapper = Wrappers.lambdaQuery();
        if (tenantId != null)
        {
            wrapper.eq(ParkingPayment::getTenantId, tenantId);
        }
        if (lotId != null)
        {
            wrapper.eq(ParkingPayment::getLotId, lotId);
        }
        wrapper.eq(ParkingPayment::getPayStatus, ParkingConstants.PAY_STATUS_SUCCESS)
                .ge(ParkingPayment::getPaySuccessTime, beginTime)
                .lt(ParkingPayment::getPaySuccessTime, endTime);
        return parkingPaymentMapper.selectCount(wrapper).intValue();
    }

    private BigDecimal calcCompare(BigDecimal current, BigDecimal previous)
    {
        current = defaultAmount(current);
        previous = defaultAmount(previous);
        if (previous.compareTo(BigDecimal.ZERO) == 0)
        {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        }
        return current.subtract(previous).multiply(BigDecimal.valueOf(100)).divide(previous, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumIncome(List<ParkingDailyStat> records)
    {
        return records.stream().map(ParkingDailyStat::getIncomeAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ParkingDailyStat> mergeDailyStatsByLot(List<ParkingDailyStat> records, ParkingDailyStat query)
    {
        if (CollectionUtils.isEmpty(records))
        {
            return new ArrayList<>();
        }
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        Date beginDate = queryBeginDate(query, startOfDay(new Date()));
        Date endDate = queryEndDate(query, startOfDay(new Date()));
        Map<Long, List<ParkingDailyStat>> recordMap = records.stream()
                .collect(Collectors.groupingBy(ParkingDailyStat::getLotId));
        return recordMap.values().stream()
                .map(items -> mergeDailyStats(items, tenantId, beginDate, endDate))
                .sorted(Comparator.comparing(ParkingDailyStat::getStatDate, Comparator.nullsLast(Date::compareTo)).reversed()
                        .thenComparing(ParkingDailyStat::getLotId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private ParkingDailyStat mergeDailyStats(List<ParkingDailyStat> records, Long tenantId, Date beginDate, Date endDate)
    {
        ParkingDailyStat first = records.get(0);
        ParkingDailyStat stat = new ParkingDailyStat();
        stat.setTenantId(first.getTenantId());
        stat.setLotId(first.getLotId());
        ParkingDailyStat latest = records.stream()
                .filter(item -> item.getStatDate() != null)
                .max(Comparator.comparing(ParkingDailyStat::getStatDate))
                .orElse(first);
        stat.setStatDate(latest.getStatDate());
        stat.setEntryCount(records.stream().map(ParkingDailyStat::getEntryCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum());
        stat.setExitCount(records.stream().map(ParkingDailyStat::getExitCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum());
        stat.setInParkCount(defaultInt(latest.getInParkCount()));
        stat.setOrderCount(records.stream().map(ParkingDailyStat::getOrderCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum());
        stat.setPaidOrderCount(countPaidPayment(tenantId, stat.getLotId(), beginDate, endDate));
        stat.setIncomeAmount(sumPaidAmount(tenantId, stat.getLotId(), beginDate, endDate));
        stat.setAvgOrderAmount(defaultInt(stat.getPaidOrderCount()) == 0
                ? BigDecimal.ZERO
                : stat.getIncomeAmount().divide(BigDecimal.valueOf(stat.getPaidOrderCount()), 2, RoundingMode.HALF_UP));
        stat.setTurnoverRate(avg(records, ParkingDailyStat::getTurnoverRate));
        stat.setCreateTime(records.stream().map(ParkingDailyStat::getCreateTime).filter(Objects::nonNull).min(Date::compareTo).orElse(null));
        stat.setUpdateTime(records.stream().map(ParkingDailyStat::getUpdateTime).filter(Objects::nonNull).max(Date::compareTo).orElse(null));
        return stat;
    }

    private IPage<ParkingDailyStat> pageList(List<ParkingDailyStat> records)
    {
        long pageNum = Convert.toLong(ServletUtils.getParameterToInt(MybatisPageConstants.PAGE_NUM), 1L);
        long pageSize = Convert.toLong(ServletUtils.getParameterToInt(MybatisPageConstants.PAGE_SIZE), 10L);
        Page<ParkingDailyStat> page = new Page<>(pageNum, pageSize);
        page.setTotal(records.size());
        int fromIndex = (int) Math.min((pageNum - 1) * pageSize, records.size());
        int toIndex = (int) Math.min(fromIndex + pageSize, records.size());
        page.setRecords(records.subList(fromIndex, toIndex));
        return page;
    }

    private int sumOrders(List<ParkingDailyStat> records)
    {
        return records.stream().map(ParkingDailyStat::getOrderCount).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

    private BigDecimal avg(List<ParkingDailyStat> records, Function<ParkingDailyStat, BigDecimal> getter)
    {
        List<BigDecimal> values = records.stream().map(getter).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(values))
        {
            return BigDecimal.ZERO;
        }
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private Date parseDate(String value, Date defaultValue)
    {
        if (value == null || value.trim().isEmpty())
        {
            return defaultValue;
        }
        try
        {
            return DATE_TIME_FORMAT.parse(value);
        }
        catch (ParseException e)
        {
            return defaultValue;
        }
    }

    private Date statBeginDate(ParkingDailyStat query, Date defaultValue)
    {
        Object beginDate = query.getParams().get("beginStatDate");
        return parseDate(String.valueOf(beginDate == null ? "" : beginDate) + " 00:00:00", defaultValue);
    }

    private Date queryBeginDate(ParkingDailyStat query, Date defaultValue)
    {
        return statBeginDate(query, defaultValue);
    }

    private Date statEndDate(ParkingDailyStat query, Date defaultValue)
    {
        Object endDate = query.getParams().get("endStatDate");
        return parseDate(String.valueOf(endDate == null ? "" : endDate) + " 00:00:00", defaultValue);
    }

    private Date queryEndDate(ParkingDailyStat query, Date defaultValue)
    {
        return endOfDay(statEndDate(query, defaultValue));
    }

    private Date startOfDay(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date endOfDay(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startOfDay(date));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        return calendar.getTime();
    }

    private Date addDays(Date date, int days)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    private Date toDate(LocalDate date)
    {
        return Date.from(date.atStartOfDay(ZONE_ID).toInstant());
    }

    private LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZONE_ID).toLocalDate();
    }

    private Integer defaultInt(Integer value)
    {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultAmount(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void enrichLotName(List<ParkingDailyStat> records)
    {
        if (records == null || records.isEmpty())
        {
            return;
        }
        Set<Long> lotIds = records.stream().map(ParkingDailyStat::getLotId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (lotIds.isEmpty())
        {
            return;
        }
        Map<Long, String> lotNameMap = parkingLotMapper.selectList(Wrappers.<ParkingLot>lambdaQuery().in(ParkingLot::getLotId, lotIds))
                .stream().collect(Collectors.toMap(ParkingLot::getLotId, ParkingLot::getLotName, (a, b) -> a));
        for (ParkingDailyStat record : records)
        {
            record.setLotName(lotNameMap.get(record.getLotId()));
        }
    }
}
