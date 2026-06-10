// 密码管理系统助手 - 弹出窗口脚本

document.addEventListener('DOMContentLoaded', () => {
    // 元素引用
    const loginForm = document.getElementById('loginForm');
    const loggedIn = document.getElementById('loggedIn');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.getElementById('loginBtn');
    const errorMessage = document.getElementById('errorMessage');
    const userAvatar = document.getElementById('userAvatar');
    const usernameDisplay = document.getElementById('usernameDisplay');
    const logoutBtn = document.getElementById('logoutBtn');
    const currentDomain = document.getElementById('currentDomain');
    const credentialStatus = document.getElementById('credentialStatus');
    const credentialsList = document.getElementById('credentialsList');
    const addCredentialBtn = document.getElementById('addCredentialBtn');
    const openManagerBtn = document.getElementById('openManagerBtn');
    const generatePasswordBtn = document.getElementById('generatePasswordBtn');
    const settingsBtn = document.getElementById('settingsBtn');

    let currentTab = null;
    let currentCredentials = [];

    // 初始化
    init();

    async function init() {
        // 检查登录状态
        const loginStatus = await checkLoginStatus();

        if (loginStatus.loggedIn) {
            showLoggedInState(loginStatus.data);
            await loadCurrentTabInfo();
        } else {
            showLoginForm();
        }

        // 绑定事件
        bindEvents();
    }

    // 绑定事件
    function bindEvents() {
        // 登录按钮
        loginBtn.addEventListener('click', handleLogin);

        // 回车键登录
        passwordInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                handleLogin();
            }
        });

        // 退出按钮
        logoutBtn.addEventListener('click', handleLogout);

        // 添加凭证按钮
        addCredentialBtn.addEventListener('click', () => {
            chrome.tabs.create({ url: 'http://localhost:5173/credentials' });
        });

        // 打开管理按钮
        openManagerBtn.addEventListener('click', () => {
            chrome.tabs.create({ url: 'http://localhost:5173' });
        });

        // 生成密码按钮
        generatePasswordBtn.addEventListener('click', () => {
            chrome.tabs.create({ url: 'http://localhost:5173/generator' });
        });

        // 设置按钮
        settingsBtn.addEventListener('click', () => {
            chrome.tabs.create({ url: 'http://localhost:5173/settings' });
        });
    }

    // 检查登录状态
    async function checkLoginStatus() {
        return new Promise((resolve) => {
            chrome.runtime.sendMessage({ action: 'checkLoginStatus' }, (response) => {
                resolve(response || { success: false, loggedIn: false });
            });
        });
    }

    // 处理登录
    async function handleLogin() {
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        if (!username || !password) {
            showError('请输入用户名和密码');
            return;
        }

        loginBtn.classList.add('loading');
        loginBtn.disabled = true;
        hideError();

        try {
            const response = await new Promise((resolve, reject) => {
                chrome.runtime.sendMessage({
                    action: 'login',
                    data: { username, password }
                }, (response) => {
                    if (response.success) {
                        resolve(response);
                    } else {
                        reject(new Error(response.error));
                    }
                });
            });

            showLoggedInState(response.data.userInfo);
            await loadCurrentTabInfo();
        } catch (error) {
            showError(error.message || '登录失败');
        } finally {
            loginBtn.classList.remove('loading');
            loginBtn.disabled = false;
        }
    }

    // 处理退出
    async function handleLogout() {
        await chrome.storage.local.remove(['authToken']);
        showLoginForm();
    }

    // 显示登录表单
    function showLoginForm() {
        loginForm.style.display = 'block';
        loggedIn.style.display = 'none';
    }

    // 显示已登录状态
    function showLoggedInState(userInfo) {
        loginForm.style.display = 'none';
        loggedIn.style.display = 'block';

        if (userInfo) {
            userAvatar.textContent = userInfo.username.charAt(0).toUpperCase();
            usernameDisplay.textContent = userInfo.username;
        }
    }

    // 加载当前标签页信息
    async function loadCurrentTabInfo() {
        try {
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            currentTab = tab;

            if (tab && tab.url) {
                const url = new URL(tab.url);
                const domain = url.hostname;
                currentDomain.textContent = domain;

                // 获取该域名的凭证
                await loadCredentials(domain);
            } else {
                currentDomain.textContent = '-';
                credentialStatus.textContent = '无法获取网站信息';
            }
        } catch (error) {
            console.error('获取标签页信息失败:', error);
            currentDomain.textContent = '-';
            credentialStatus.textContent = '获取信息失败';
        }
    }

    // 加载凭证
    async function loadCredentials(domain) {
        try {
            credentialStatus.textContent = '检测中...';

            const response = await new Promise((resolve, reject) => {
                chrome.runtime.sendMessage({
                    action: 'getCredentials',
                    domain: domain
                }, (response) => {
                    if (response.success) {
                        resolve(response);
                    } else {
                        reject(new Error(response.error));
                    }
                });
            });

            currentCredentials = response.data || [];

            if (currentCredentials.length > 0) {
                credentialStatus.textContent = `发现 ${currentCredentials.length} 个账号`;
                credentialStatus.classList.add('has-credential');
                renderCredentials(currentCredentials);
            } else {
                credentialStatus.textContent = '未找到保存的账号';
                credentialStatus.classList.remove('has-credential');
                renderEmptyState();
            }
        } catch (error) {
            console.error('获取凭证失败:', error);
            credentialStatus.textContent = '获取凭证失败';
            credentialStatus.classList.remove('has-credential');
            renderEmptyState();
        }
    }

    // 渲染凭证列表
    function renderCredentials(credentials) {
        credentialsList.innerHTML = '';

        credentials.forEach((cred, index) => {
            const item = document.createElement('div');
            item.className = 'credential-item';
            item.innerHTML = `
                <div class="credential-username">${cred.username}</div>
                <div class="credential-actions">
                    <button class="action-btn fill-btn" data-index="${index}">一键填充</button>
                    <button class="action-btn copy-btn" data-index="${index}">复制密码</button>
                </div>
            `;
            credentialsList.appendChild(item);
        });

        // 绑定按钮事件
        credentialsList.querySelectorAll('.fill-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const index = parseInt(btn.dataset.index);
                fillCredential(credentials[index]);
            });
        });

        credentialsList.querySelectorAll('.copy-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const index = parseInt(btn.dataset.index);
                copyPassword(credentials[index].password);
            });
        });
    }

    // 渲染空状态
    function renderEmptyState() {
        credentialsList.innerHTML = `
            <div class="empty-state">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#909399" stroke-width="1.5">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                </svg>
                <p>暂无可用账号</p>
            </div>
        `;
    }

    // 填充凭证
    async function fillCredential(credential) {
        if (!currentTab) {
            showError('无法获取当前标签页');
            return;
        }

        try {
            await new Promise((resolve, reject) => {
                chrome.runtime.sendMessage({
                    action: 'fillCredentials',
                    data: credential
                }, (response) => {
                    if (response.success) {
                        resolve(response);
                    } else {
                        reject(new Error(response.error));
                    }
                });
            });

            showSuccess('账号密码已填充');
            window.close();
        } catch (error) {
            showError(error.message || '填充失败');
        }
    }

    // 复制密码
    async function copyPassword(password) {
        try {
            await navigator.clipboard.writeText(password);
            showSuccess('密码已复制到剪贴板');
        } catch (error) {
            showError('复制失败');
        }
    }

    // 显示错误信息
    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.classList.add('show');
    }

    // 隐藏错误信息
    function hideError() {
        errorMessage.classList.remove('show');
    }

    // 显示成功信息
    function showSuccess(message) {
        // 创建临时提示
        const toast = document.createElement('div');
        toast.style.cssText = `
            position: fixed;
            top: 10px;
            left: 50%;
            transform: translateX(-50%);
            background: #f0f9eb;
            color: #67c23a;
            padding: 8px 16px;
            border-radius: 4px;
            font-size: 13px;
            z-index: 1000;
            animation: fadeInOut 2s ease;
        `;
        toast.textContent = message;
        document.body.appendChild(toast);

        setTimeout(() => toast.remove(), 2000);
    }
});