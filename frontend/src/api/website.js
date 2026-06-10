import request from './request'

/**
 * 获取网站列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getWebsiteList(params) {
    return request({
        url: '/website/list',
        method: 'get',
        params
    })
}

/**
 * 获取网站详情
 * @param {Number} id 网站ID
 * @returns {Promise}
 */
export function getWebsiteDetail(id) {
    return request({
        url: `/website/${id}`,
        method: 'get'
    })
}

/**
 * 创建网站
 * @param {Object} data 网站信息
 * @returns {Promise}
 */
export function createWebsite(data) {
    return request({
        url: '/website',
        method: 'post',
        data
    })
}

/**
 * 更新网站
 * @param {Number} id 网站ID
 * @param {Object} data 网站信息
 * @returns {Promise}
 */
export function updateWebsite(id, data) {
    return request({
        url: `/website/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除网站
 * @param {Number} id 网站ID
 * @returns {Promise}
 */
export function deleteWebsite(id) {
    return request({
        url: `/website/${id}`,
        method: 'delete'
    })
}

/**
 * 根据域名查询网站
 * @param {String} domain 域名
 * @returns {Promise}
 */
export function getWebsiteByDomain(domain) {
    return request({
        url: '/website/domain',
        method: 'get',
        params: { domain }
    })
}