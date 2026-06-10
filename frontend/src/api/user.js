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
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
    return request({
        url: '/auth/logout',
        method: 'post'
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
 * 验证主密码
 * @param {Object} data 主密码信息
 * @returns {Promise}
 */
export function verifyMasterPassword(data) {
    return request({
        url: '/user/verify-master-password',
        method: 'post',
        data
    })
}