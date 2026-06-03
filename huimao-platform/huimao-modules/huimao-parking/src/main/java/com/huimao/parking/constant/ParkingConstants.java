package com.huimao.parking.constant;

/**
 * 停车业务常量。
 *
 * <p>当前版本为了落地速度，状态值先统一使用 String 常量，而不是独立枚举类型。
 * 这样做的代价是编译期约束较弱，所以这里把每个值的业务语义补充完整，避免后续维护时误用。</p>
 */
public interface ParkingConstants
{
    /**
     * 逻辑删除标记：0 表示记录有效。
     */
    String DEL_FLAG_NORMAL = "0";

    /**
     * 逻辑删除标记：2 表示记录已逻辑删除。
     */
    String DEL_FLAG_DELETED = "2";

    /**
     * 停车场状态：0 表示运营中/启用。
     */
    String LOT_STATUS_ENABLED = "0";

    /**
     * 停车场状态：1 表示停用。
     */
    String LOT_STATUS_DISABLED = "1";

    /**
     * 通道类型：1 表示入口通道。
     */
    String GATE_TYPE_ENTRY = "1";

    /**
     * 通道类型：2 表示出口通道。
     */
    String GATE_TYPE_EXIT = "2";

    /**
     * 通用状态：0 表示启用。
     *
     * <p>用于设备、收费规则等需要简单启停控制的对象。</p>
     */
    String COMMON_STATUS_ENABLED = "0";

    /**
     * 通用状态：1 表示停用。
     */
    String COMMON_STATUS_DISABLED = "1";

    /**
     * 停车会话状态：车辆已入场，当前仍在场内，尚未触发支付。
     */
    String SESSION_STATUS_IN_PARK = "IN_PARK";

    /**
     * 停车会话状态：已生成待支付订单，车辆离场前必须先缴费。
     */
    String SESSION_STATUS_WAIT_PAY = "WAIT_PAY";

    /**
     * 停车会话状态：订单已支付，处于离场宽限期内，出口识别后可直接放行。
     */
    String SESSION_STATUS_PAID_PENDING_EXIT = "PAID_PENDING_EXIT";

    /**
     * 停车会话状态：车辆已完成离场，整个停车流程闭环结束。
     */
    String SESSION_STATUS_COMPLETED = "COMPLETED";

    /**
     * 停车会话状态：异常会话。
     *
     * <p>例如重复入场、设备上报异常、人工介入等场景。</p>
     */
    String SESSION_STATUS_ABNORMAL = "ABNORMAL";

    /**
     * 订单类型：正常停车订单。
     *
     * <p>通常为入场后的第一笔应收订单。</p>
     */
    String ORDER_TYPE_NORMAL = "NORMAL";

    /**
     * 订单类型：超时补费订单。
     *
     * <p>典型场景为支付后超出宽限期仍未离场，需要重新补缴差额。</p>
     */
    String ORDER_TYPE_OVERTIME = "OVERTIME";

    /**
     * 订单状态：未支付。
     */
    String ORDER_STATUS_UNPAID = "UNPAID";

    /**
     * 订单状态：已支付。
     */
    String ORDER_STATUS_PAID = "PAID";

    /**
     * 订单状态：已关闭。
     *
     * <p>预留给取消、人工关闭、失效等场景使用。</p>
     */
    String ORDER_STATUS_CLOSED = "CLOSED";

    /**
     * 支付状态：未支付。
     */
    String PAY_STATUS_UNPAID = "UNPAID";

    /**
     * 支付状态：支付成功。
     */
    String PAY_STATUS_SUCCESS = "SUCCESS";

    /**
     * 指令类型：开闸。
     *
     * <p>当前首版仅支持这一种设备下行指令。</p>
     */
    String COMMAND_TYPE_OPEN_GATE = "OPEN_GATE";

    /**
     * 指令状态：已创建，尚未确认执行结果。
     */
    String COMMAND_STATUS_INIT = "INIT";

    /**
     * 指令状态：执行成功。
     */
    String COMMAND_STATUS_SUCCESS = "SUCCESS";

    /**
     * 指令状态：执行失败。
     */
    String COMMAND_STATUS_FAIL = "FAIL";

    /**
     * 账户状态：正常。
     */
    String ACCOUNT_STATUS_NORMAL = "0";

    /**
     * 账户状态：冻结。
     */
    String ACCOUNT_STATUS_FROZEN = "1";

    /**
     * 提现状态：待审核。
     */
    String WITHDRAW_STATUS_WAIT_AUDIT = "WAIT_AUDIT";

    /**
     * 提现状态：已打款。
     */
    String WITHDRAW_STATUS_PAID = "PAID";

    /**
     * 提现状态：已驳回。
     */
    String WITHDRAW_STATUS_REJECTED = "REJECTED";

    /**
     * 账户流水业务类型：停车收入入账。
     */
    String ACCOUNT_BIZ_TYPE_PARKING_INCOME = "PARKING_INCOME";

    /**
     * 账户流水业务类型：提现申请冻结。
     */
    String ACCOUNT_BIZ_TYPE_WITHDRAW_FREEZE = "WITHDRAW_FREEZE";

    /**
     * 账户流水业务类型：提现打款出账。
     */
    String ACCOUNT_BIZ_TYPE_WITHDRAW_PAY = "WITHDRAW_PAY";

    /**
     * 账户流水业务类型：提现驳回退回。
     */
    String ACCOUNT_BIZ_TYPE_WITHDRAW_REJECT = "WITHDRAW_REJECT";

    /**
     * 设备事件类型：入口识别事件。
     */
    String EVENT_TYPE_ENTRY = "ENTRY";

    /**
     * 设备事件类型：出口识别事件。
     */
    String EVENT_TYPE_EXIT = "EXIT";

    /**
     * 设备事件类型：设备心跳。
     */
    String EVENT_TYPE_HEARTBEAT = "HEARTBEAT";

    /**
     * 设备事件类型：开闸结果回传。
     */
    String EVENT_TYPE_OPEN_RESULT = "OPEN_RESULT";
}
