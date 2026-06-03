function sizeValidator(value) {
  const methodTypes = ['large', 'small', 'mini'];
  const valid = methodTypes.indexOf(value.toLowerCase()) !== -1 || value === '';
  if (!valid) {
    throw new Error(`Size must be one of ['large', 'small', 'mini']`);
  }
  return valid;
}

export const formProps = {
  value: Object,
  size: {
    type: String,
    default: 'small',
    validator: sizeValidator
  },
  className:{
    type: String,
    default: '',
  },
  labelWidth: Number,
  loading: {
    type: Boolean,
    default: false
  },
  // 查看更多查询条件
  showMoreBtn: {
    type: Boolean,
    default: false
  },
  submitBtnText: {
    type: String,
    default: '查询'
  },
  hideResetBtn: {
    type: Boolean,
    default: false
  },
  btnHandle: {
    type:Boolean,
    default: false
  },
  resetBtnText: {
    type: String,
    default: '重置'
  },
  forms: {
    type: Array,
    required: true
    // label: String,
    // prop: {
    //   type: [String, Array],
    //   required: true
    // },
    // itemType: {
    //   type: String,
    //   default: 'input',
    //   validator: itemTypeValidator
    // },
    // size: {
    //   type: String,
    //   default: '',
    //   validator: sizeValidator
    // },
    // placeholder: {
    //   type: String,
    //   default: ''
    // },
    // editable: {
    //   type: Boolean,
    //   default: true
    // },
    // disabled: {
    //   type: Boolean,
    //   default: false
    // },
    // readonly: {
    //   type: Boolean,
    //   default: false
    // },
    // fuzzy: {
    //   type: Boolean,
    //   default: false
    // },
    // options: Array,
    // selectFetch: Function,
    // selectResultField: String,
    // selectResultHandler: Function,
    // selectUrl: String,
    // selectMethod: String,
    // valueKey: String,
    // labelKey: String,
    // format: Function,
    // selectParams: {
    //   type: Object,
    //   default: () => {
    //     return {}
    //   }
    // },
    // rules: Array,
    // pickerOptions: Object
  }
};
