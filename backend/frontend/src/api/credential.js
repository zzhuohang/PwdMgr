import request from './request'

/**
 * 获取凭证列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getCredentialList(params) {
    return request({
        url: '/credentials',
        method: 'get',
        params
    })
}

/**
 * 获取凭证详情（解密）
 * @param {Number} id 凭证ID
 * @param {String} masterPassword 主密码
 * @returns {Promise}
 */
export function getCredentialDetail(id, masterPassword) {
    return request({
        url: `/credentials/${id}`,
        method: 'get',
        params: { masterPassword }
    })
}

/**
 * 创建凭证
 * @param {Object} data 凭证信息
 * @param {String} masterPassword 主密码
 * @returns {Promise}
 */
export function createCredential(data, masterPassword) {
    return request({
        url: '/credentials',
        method: 'post',
        data,
        params: { masterPassword }
    })
}

/**
 * 更新凭证
 * @param {Number} id 凭证ID
 * @param {Object} data 凭证信息
 * @param {String} masterPassword 主密码
 * @returns {Promise}
 */
export function updateCredential(id, data, masterPassword) {
    return request({
        url: `/credentials/${id}`,
        method: 'put',
        data,
        params: { masterPassword }
    })
}

/**
 * 删除凭证
 * @param {Number} id 凭证ID
 * @returns {Promise}
 */
export function deleteCredential(id) {
    return request({
        url: `/credentials/${id}`,
        method: 'delete'
    })
}

/**
 * 根据域名获取凭证（解密）
 * @param {String} domain 域名
 * @param {String} masterPassword 主密码
 * @returns {Promise}
 */
export function getCredentialsByDomain(domain, masterPassword) {
    return request({
        url: `/credentials/domain/${domain}`,
        method: 'get',
        params: { masterPassword }
    })
}

/**
 * 获取凭证统计信息
 * @returns {Promise}
 */
export function getCredentialStats() {
    return request({
        url: '/credentials/stats',
        method: 'get'
    })
}