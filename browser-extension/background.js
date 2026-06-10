// 密码管理系统助手 - 后台脚本

// API配置
const API_BASE_URL = 'http://localhost:8880/api';

// 存储token
let authToken = '';

// 监听安装事件
chrome.runtime.onInstalled.addListener(() => {
    console.log('密码管理系统助手已安装');
});

// 监听消息
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    switch (request.action) {
        case 'login':
            handleLogin(request.data)
                .then(sendResponse)
                .catch(error => sendResponse({ success: false, error: error.message }));
            return true;

        case 'getCredentials':
            getCredentials(request.domain, request.masterPassword)
                .then(sendResponse)
                .catch(error => sendResponse({ success: false, error: error.message }));
            return true;

        case 'checkLoginStatus':
            checkLoginStatus()
                .then(sendResponse)
                .catch(error => sendResponse({ success: false, error: error.message }));
            return true;

        case 'fillCredentials':
            fillCredentials(sender.tab.id, request.data)
                .then(sendResponse)
                .catch(error => sendResponse({ success: false, error: error.message }));
            return true;

        case 'saveCredential':
            saveCredential(request.data, request.masterPassword)
                .then(sendResponse)
                .catch(error => sendResponse({ success: false, error: error.message }));
            return true;

        case 'openPopup':
            chrome.action.openPopup();
            break;
    }
});

// 处理登录
async function handleLogin(data) {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.code === 200) {
            authToken = result.data.token;
            // 保存token到storage
            await chrome.storage.local.set({ authToken: authToken });
            return { success: true, data: result.data };
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('登录失败:', error);
        throw error;
    }
}

// 获取凭证
async function getCredentials(domain, masterPassword) {
    try {
        const token = await getAuthToken();
        if (!token) {
            throw new Error('未登录');
        }

        const response = await fetch(`${API_BASE_URL}/credentials/domain/${encodeURIComponent(domain)}?masterPassword=${encodeURIComponent(masterPassword)}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();

        if (result.code === 200) {
            return { success: true, data: result.data };
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('获取凭证失败:', error);
        throw error;
    }
}

// 检查登录状态
async function checkLoginStatus() {
    try {
        const token = await getAuthToken();
        if (!token) {
            return { success: false, loggedIn: false };
        }

        const response = await fetch(`${API_BASE_URL}/user/info`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();

        if (result.code === 200) {
            return { success: true, loggedIn: true, data: result.data };
        } else {
            return { success: true, loggedIn: false };
        }
    } catch (error) {
        return { success: true, loggedIn: false };
    }
}

// 填充凭证
async function fillCredentials(tabId, data) {
    try {
        await chrome.tabs.sendMessage(tabId, {
            action: 'fillCredentials',
            data: data
        });
        return { success: true };
    } catch (error) {
        console.error('填充凭证失败:', error);
        throw error;
    }
}

// 保存凭证
async function saveCredential(data, masterPassword) {
    try {
        const token = await getAuthToken();
        if (!token) {
            throw new Error('未登录');
        }

        const response = await fetch(`${API_BASE_URL}/credentials?masterPassword=${encodeURIComponent(masterPassword)}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.code === 200) {
            return { success: true, data: result.data };
        } else {
            throw new Error(result.message);
        }
    } catch (error) {
        console.error('保存凭证失败:', error);
        throw error;
    }
}

// 获取auth token
async function getAuthToken() {
    if (authToken) {
        return authToken;
    }

    const result = await chrome.storage.local.get(['authToken']);
    authToken = result.authToken;
    return authToken;
}

// 监听标签页更新
chrome.tabs.onUpdated.addListener(async (tabId, changeInfo, tab) => {
    if (changeInfo.status === 'complete' && tab.url) {
        const url = new URL(tab.url);
        const domain = url.hostname;

        // 检查是否是登录页面
        const isLoginPage = checkIfLoginPage(tab.url, tab.title);

        if (isLoginPage) {
            // 获取主密码（如果已保存）
            const result = await chrome.storage.local.get(['masterPassword']);
            const masterPassword = result.masterPassword;

            if (masterPassword) {
                // 获取该域名的凭证
                try {
                    const credResult = await getCredentials(domain, masterPassword);
                    if (credResult.success && credResult.data && credResult.data.length > 0) {
                        // 通知content script显示提示
                        chrome.tabs.sendMessage(tabId, {
                            action: 'showCredentialHint',
                            data: credResult.data
                        });
                    }
                } catch (error) {
                    console.error('获取凭证失败:', error);
                }
            }
        }
    }
});

// 检查是否是登录页面
function checkIfLoginPage(url, title) {
    const loginKeywords = [
        'login',
        'signin',
        'sign-in',
        'log-in',
        'auth',
        'passport',
        'account/login',
        'user/login',
        '登录',
        '登陆'
    ];

    const urlLower = url.toLowerCase();
    const titleLower = (title || '').toLowerCase();

    return loginKeywords.some(keyword =>
        urlLower.includes(keyword) || titleLower.includes(keyword)
    );
}