export function checkVIN(VIN) {
    if (typeof (VIN) != 'string') return false;
    if (VIN.length !== 17) return false;
    VIN = VIN.toUpperCase();
    let RE = /^[A-HJ-NPR-Z\d]{8}[X\d][A-HJ-NPR-Z\d]{3}\d{5}$/
    if (!RE.test(VIN)) return false;
    let cOT = {
        '0': 0, '1': 1, '2': 2, '3': 3, '4': 4, '5': 5, '6': 6, '7': 7,
        '8': 8, '9': 9, 'A': 1, 'B': 2, 'C': 3, 'D': 4, 'E': 5, 'F': 6,
        'G': 7, 'H': 8, 'J': 1, 'K': 2, 'L': 3, 'M': 4, 'N': 5, 'P': 7,
        'R': 9, 'S': 2, 'T': 3, 'U': 4, 'V': 5, 'W': 6, 'X': 7, 'Y': 8,
        'Z': 9
    };
    let xWT = [8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2]; // 因为第9位权重为0，所以原来是什么字符不影响结果，可以方便后面计算
    let sum = 0;
    let VINs = VIN.split('');
    for (let i = 0; i < 17; i++) {
        sum = sum + cOT[VINs[i]] * xWT[i];
    }
    let cT = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X'];
    if (cT[(sum % 11)] === VINs[8]) return true;
    return false;
}