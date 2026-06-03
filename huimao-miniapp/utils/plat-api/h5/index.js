import * as wxApi from '../wx-public'
import jsQR from 'jsqr'

/**
 * 统一扫码方法（自动区分：微信浏览器 / 普通浏览器）
 * 用法和你原来的 uni.scanCode 完全一样
 */
export const scanCode = () => {
  return new Promise((resolve, reject) => {
    const isWechatH5 = /MicroMessenger/i.test(navigator.userAgent)
    const isH5 = typeof window !== 'undefined'

    if (!isH5) {
      // 非H5环境（小程序/APP）直接用uni.scanCode
      uni.scanCode({
        success: res => resolve(res),
        fail: err => reject(err)
      })
      return
    }

    if (isWechatH5) {
      wxApi.scanCode()
        .then(res => resolve({ result: res.resultStr || res.result }))
        .catch(reject)
    } else {
      browserScanCode()
        .then(code => resolve({ result: code }))
        .catch(reject)
    }
  })
}

/**
 * 普通浏览器扫码（修复版，可直接运行）
 */
function browserScanCode() {
  return new Promise((resolve, reject) => {
    // 必须是用户交互事件才能打开摄像头
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      return reject(new Error('当前浏览器不支持摄像头'))
    }

    // 检查是否已经存在扫码层，避免重复
    const oldScan = document.getElementById('custom-scan-box')
    if (oldScan) oldScan.remove()

    // ================== 创建界面 ==================
    const scanBox = document.createElement('div')
    scanBox.id = 'custom-scan-box'
    scanBox.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: #000;
      z-index: 999999;
    `

    const video = document.createElement('video')
    video.style.cssText = `
      width: 100%;
      height: 100%;
      object-fit: cover;
    `
    video.setAttribute('playsinline', true)
    video.setAttribute('webkit-playsinline', true)

    const canvas = document.createElement('canvas')
    canvas.style.display = 'none'

    const closeBtn = document.createElement('div')
    closeBtn.innerText = '关闭'
    closeBtn.style.cssText = `
      position: absolute;
      top: 20px;
      right: 20px;
      color: #fff;
      font-size: 16px;
      padding: 10px 15px;
      background: rgba(0,0,0,0.5);
      border-radius: 6px;
      z-index: 10;
    `

    scanBox.appendChild(video)
    scanBox.appendChild(canvas)
    scanBox.appendChild(closeBtn)
    document.body.appendChild(scanBox)

    let scanning = true
    let ctx = canvas.getContext('2d')
    let stream = null

    // 关闭
    function stopScan() {
      scanning = false
      if (stream) stream.getTracks().forEach(t => t.stop())
      const box = document.getElementById('custom-scan-box')
      if (box) box.remove()
    }

    closeBtn.onclick = () => {
      stopScan()
      reject(new Error('用户取消扫码'))
    }

    // 打开摄像头
    navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } }
    }).then(s => {
      stream = s
      video.srcObject = s
      video.play()
      scanLoop()
    }).catch(err => {
      stopScan()
      console.error(err)
      if (err.name === 'NotAllowedError') {
        reject(new Error('请允许摄像头权限'))
      } else {
        reject(new Error('摄像头打开失败'))
      }
    })

    // 循环扫码
    function scanLoop() {
      if (!scanning) return

      if (video.readyState === 4) {
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

        const data = ctx.getImageData(0, 0, canvas.width, canvas.height)
        const code = jsQR(data.data, data.width, data.height)

        if (code) {
          stopScan()
          resolve(code.data)
          return
        }
      }

      requestAnimationFrame(scanLoop)
    }
  })
}

/**
 * @param {String} type wgs84/gcj02
 */
export const getLocation = (type = 'wgs84') => {
  return new Promise((resolve, reject) => {
    const isWechatH5 = /MicroMessenger/i.test(navigator.userAgent)

    if (isWechatH5) {
      wxApi.getLocation(type)
        .then(resolve)
        .catch(() => {
          getLocationNormal(type).then(resolve).catch(reject)
        })
    } else {
      getLocationNormal(type).then(resolve).catch(reject)
    }
  })
}

function getLocationNormal(type) {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      return reject(new Error('当前浏览器不支持定位'))
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        resolve({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
          type: 'wgs84'
        })
      },
      (err) => {
        reject(new Error(`定位失败: ${err.message}`))
      },
      {
        enableHighAccuracy: true,
        timeout: 8000,
        maximumAge: 0
      }
    )
  })
}