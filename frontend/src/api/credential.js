import request from './request'

/**
 * 获取凭证列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getCredentialList(params) {
    return request({
        url: '/credential/list',
        method: 'get',
        params
    })
}

/**
 * 获取凭证详情
 * @param {Number} id 凭证ID
 * @returns {Promise}
 */
export function getCredentialDetail(id) {
    return request({
        url: `/credential/${id}`,
        method: 'get'
    })
}

/**
 * 创建凭证
 * @param {Object} data 凭证信息
 * @returns {Promise}
 */
export function createCredential(data) {
    return request({
        url: '/credential',
        method: 'post',
        data
    })
}

/**
 * 更新凭证
 * @param {Number} id 凭证ID
 * @param {Object} data 凭证信息
 * @returns {Promise}
 */
export function updateCredential(id, data) {
    return request({
        url: `/credential/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除凭证
 * @param {Number} id 凭证ID
 * @returns {Promise}
 */
export function deleteCredential(id) {
    return request({
        url: `/credential/${id}`,
        method: 'delete'
    })
}

/**
 * 根据域名获取凭证
 * @param {String} domain 域名
 * @returns {Promise}
 */
export function getCredentialsByDomain(domain) {
    return request({
        url: '/credential/domain',
        method: 'get',
        params: { domain }
    })
}

/**
 * 搜索凭证
 * @param {String} keyword 关键词
 * @returns {Promise}
 */
export function searchCredentials(keyword) {
    return request({
        url: '/credential/search',
        method: 'get',
        params: { keyword }
    })
}