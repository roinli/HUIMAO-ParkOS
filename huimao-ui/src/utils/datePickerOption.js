import moment from 'moment';

/**
 * 日期区间
 */
export const pickerRangeOptions = {
  shortcuts: [
    {
      text: '今天',
      onClick(picker) {
        const start = moment().startOf('day').toDate();
        const end = moment().endOf('day').toDate();
        picker.$emit('pick', [start, end]);
      }
    },
    {
      text: '昨天',
      onClick(picker) {
        const start = moment().subtract(1, 'days').startOf('day').toDate();
        const end = moment().subtract(1, 'days').endOf('day').toDate();
        picker.$emit('pick', [start, end]);
      }
    },
    {
      text: '近7天',
      onClick(picker) {
        const start = moment().subtract(6, 'days').startOf('day').toDate();
        const end = moment().endOf('day').toDate();
        picker.$emit('pick', [start, end]);
      }
    },
    {
      text: '近30天',
      onClick(picker) {
        const start = moment().subtract(29, 'days').startOf('day').toDate();
        const end = moment().endOf('day').toDate();
        picker.$emit('pick', [start, end]);
      }
    },
    {
      text: '近三个月',
      onClick(picker) {
        const start = moment().subtract(2, 'months').startOf('month').toDate();
        const end = moment().endOf('day').toDate();
        picker.$emit('pick', [start, end]);
      }
    },
    {
      text: '近一年',
      onClick(picker) {
        const start = moment().subtract(1, 'years').add(1, 'days').startOf('day').toDate();
        const end = moment().endOf('day').toDate();
        picker.$emit('pick', [start, end]);
      }
    }
  ]
};

/**
 * 日期单选
 */
export const pickerSingleOptions = {
  shortcuts: [
    {
      text: '今天',
      onClick(picker) {
        const date = moment().startOf('day').toDate();
        picker.$emit('pick', date);
      }
    },
    {
      text: '昨天',
      onClick(picker) {
        const date = moment().subtract(1, 'days').startOf('day').toDate();
        picker.$emit('pick', date);
      }
    },
    {
      text: '一周前',
      onClick(picker) {
        const date = moment().subtract(7, 'days').startOf('day').toDate();
        picker.$emit('pick', date);
      }
    },
    {
      text: '一个月前',
      onClick(picker) {
        const date = moment().subtract(1, 'months').startOf('day').toDate();
        picker.$emit('pick', date);
      }
    }
  ]
};

/**
 * 严格限制间隔不超过 30 天
 */
let startDate, endDate;
export const pickerIntervalOptions = {
  // 记录开始日期
  onPick: ({ maxDate, minDate }) => {
    startDate = minDate; // 选中开始日期时记录
    endDate = maxDate; // 选中结束日期时记录

    // 如果开始日期已选，结束日期未选，则限制结束日期的可选范围
    if (minDate && !maxDate) {
      startDate = minDate;
    } else {
      startDate = null; // 重置开始日期（重新选择时生效）
    }
  },
  // 限制结束日期的可选范围（最大为开始日期 + 30天）
  disabledDate: (time) => {
    // console.log(startDate, endDate);
    if (startDate) {
      // 计算开始日期 + 30天的时间戳
      const thirtyDaysLater = new Date(startDate).getTime() + 30 * 24 * 60 * 60 * 1000;
      // 结束日期不能晚于开始日期 + 30天，且不能早于开始日期
      return time.getTime() > thirtyDaysLater || time.getTime() < startDate.getTime() - 8.64e7;
    }
  }
};

export const dateClear = () => {
  startDate = '';
  endDate = '';
};
