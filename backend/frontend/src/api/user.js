import request from './request'

/**
 * 用户登录
 * @param {Object} data 登录信息
 * @returns {Promise}
 */
export function login(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

/**
 * 用户注册
 * @param {Object} data 注册信息
 * @returns {Promise}
 */
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

/**
 * 刷新Token
 * @param {String} refreshToken 刷新Token
 * @returns {Promise}
 */
export function refreshToken(refreshToken) {
    return request({
        url: '/auth/refresh',
        method: 'post',
        params: { refreshToken }
    })
}

/**
 * 验证主密码
 * @param {String} masterPassword 主密码
 * @returns {Promise}
 */
export function verifyMasterPassword(masterPassword) {
    return request({
        url: '/auth/verify-master-password',
        method: 'post',
        params: { masterPassword }
    })
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
    return request({
        url: '/user/info',
        method: 'get'
    })
}

/**
 * 修改密码
 * @param {Object} data 密码信息
 * @returns {Promise}
 */
export function changePassword(data) {
    return request({
        url: '/user/password',
        method: 'put',
        data
    })
}

/**
 * 修改主密码
 * @param {Object} data 主密码信息
 * @returns {Promise}
 */
export function changeMasterPassword(data) {
    return request({
        url: '/user/master-password',
        method: 'put',
        data
    })
}