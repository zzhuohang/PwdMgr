// 密码管理系统助手 - 内容脚本

(function() {
    'use strict';

    // 配置
    const CONFIG = {
        HINT_CLASS_NAME: 'pwd-manager-hint',
        POPUP_CLASS_NAME: 'pwd-manager-popup',
        FILL_BUTTON_CLASS: 'pwd-manager-fill-btn'
    };

    // 监听消息
    chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
        switch (request.action) {
            case 'showCredentialHint':
                showCredentialHint(request.data);
                sendResponse({ success: true });
                break;

            case 'fillCredentials':
                fillCredentials(request.data);
                sendResponse({ success: true });
                break;
        }
    });

    // 检测登录表单
    function detectLoginForm() {
        const forms = document.querySelectorAll('form');
        const loginForms = [];

        forms.forEach(form => {
            const inputs = form.querySelectorAll('input');
            let hasUsername = false;
            let hasPassword = false;

            inputs.forEach(input => {
                const type = input.type.toLowerCase();
                const name = input.name.toLowerCase();
                const id = input.id.toLowerCase();
                const placeholder = (input.placeholder || '').toLowerCase();

                if (type === 'text' || type === 'email' || type === 'tel' ||
                    name.includes('user') || name.includes('login') || name.includes('email') ||
                    id.includes('user') || id.includes('login') || id.includes('email') ||
                    placeholder.includes('用户') || placeholder.includes('邮箱') || placeholder.includes('手机')) {
                    hasUsername = true;
                }

                if (type === 'password') {
                    hasPassword = true;
                }
            });

            if (hasUsername && hasPassword) {
                loginForms.push(form);
            }
        });

        return loginForms;
    }

    // 显示凭证提示
    function showCredentialHint(credentials) {
        // 移除已存在的提示
        removeExistingHints();

        const loginForms = detectLoginForm();

        if (loginForms.length === 0) {
            console.log('未检测到登录表单');
            return;
        }

        loginForms.forEach((form, index) => {
            const hint = createHintElement(credentials, form);
            document.body.appendChild(hint);

            // 定位提示框
            positionHint(hint, form);
        });
    }

    // 创建提示元素
    function createHintElement(credentials, form) {
        const hint = document.createElement('div');
        hint.className = CONFIG.HINT_CLASS_NAME;
        hint.style.cssText = `
            position: fixed;
            z-index: 999999;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            padding: 16px;
            min-width: 300px;
            max-width: 400px;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        `;

        // 标题
        const title = document.createElement('div');
        title.style.cssText = `
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            gap: 8px;
        `;
        title.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#409eff" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            发现保存的账号密码
        `;
        hint.appendChild(title);

        // 凭证列表
        credentials.forEach((cred, index) => {
            const credItem = createCredentialItem(cred, index, form);
            hint.appendChild(credItem);
        });

        // 关闭按钮
        const closeBtn = document.createElement('button');
        closeBtn.textContent = '×';
        closeBtn.style.cssText = `
            position: absolute;
            top: 8px;
            right: 8px;
            background: none;
            border: none;
            font-size: 20px;
            cursor: pointer;
            color: #909399;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 4px;
        `;
        closeBtn.onmouseover = () => closeBtn.style.backgroundColor = '#f5f7fa';
        closeBtn.onmouseout = () => closeBtn.style.backgroundColor = 'transparent';
        closeBtn.onclick = () => hint.remove();
        hint.appendChild(closeBtn);

        return hint;
    }

    // 创建凭证项
    function createCredentialItem(credential, index, form) {
        const item = document.createElement('div');
        item.style.cssText = `
            padding: 10px;
            margin-bottom: 8px;
            background: #f5f7fa;
            border-radius: 6px;
            cursor: pointer;
            transition: background 0.2s;
        `;
        item.onmouseover = () => item.style.backgroundColor = '#ecf5ff';
        item.onmouseout = () => item.style.backgroundColor = '#f5f7fa';

        // 用户名
        const usernameDiv = document.createElement('div');
        usernameDiv.style.cssText = `
            font-size: 14px;
            color: #303133;
            margin-bottom: 4px;
            display: flex;
            align-items: center;
            gap: 8px;
        `;
        usernameDiv.innerHTML = `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#909399" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
            </svg>
            ${credential.username}
        `;
        item.appendChild(usernameDiv);

        // 填充按钮
        const fillBtn = document.createElement('button');
        fillBtn.className = CONFIG.FILL_BUTTON_CLASS;
        fillBtn.textContent = '一键填充';
        fillBtn.style.cssText = `
            background: #409eff;
            color: #fff;
            border: none;
            padding: 6px 12px;
            border-radius: 4px;
            font-size: 12px;
            cursor: pointer;
            margin-top: 8px;
            transition: background 0.2s;
        `;
        fillBtn.onmouseover = () => fillBtn.style.backgroundColor = '#66b1ff';
        fillBtn.onmouseout = () => fillBtn.style.backgroundColor = '#409eff';
        fillBtn.onclick = (e) => {
            e.stopPropagation();
            fillCredential(credential, form);
        };
        item.appendChild(fillBtn);

        // 复制按钮
        const copyBtn = document.createElement('button');
        copyBtn.textContent = '复制密码';
        copyBtn.style.cssText = `
            background: #67c23a;
            color: #fff;
            border: none;
            padding: 6px 12px;
            border-radius: 4px;
            font-size: 12px;
            cursor: pointer;
            margin-top: 8px;
            margin-left: 8px;
            transition: background 0.2s;
        `;
        copyBtn.onmouseover = () => copyBtn.style.backgroundColor = '#85ce61';
        copyBtn.onmouseout = () => copyBtn.style.backgroundColor = '#67c23a';
        copyBtn.onclick = (e) => {
            e.stopPropagation();
            copyPassword(credential.password);
        };
        item.appendChild(copyBtn);

        return item;
    }

    // 定位提示框
    function positionHint(hint, form) {
        const rect = form.getBoundingClientRect();
        const scrollTop = window.scrollY || document.documentElement.scrollTop;
        const scrollLeft = window.scrollX || document.documentElement.scrollLeft;

        // 计算位置
        let top = rect.top + scrollTop - 10;
        let left = rect.left + scrollLeft;

        // 确保不超出视口
        const hintWidth = 300;
        const hintHeight = 200;

        if (left + hintWidth > window.innerWidth) {
            left = window.innerWidth - hintWidth - 20;
        }

        if (top + hintHeight > window.innerHeight + scrollTop) {
            top = rect.top + scrollTop - hintHeight - 10;
        }

        hint.style.top = `${top}px`;
        hint.style.left = `${left}px`;
    }

    // 填充凭证
    function fillCredential(credential, form) {
        const inputs = form.querySelectorAll('input');
        let usernameInput = null;
        let passwordInput = null;

        inputs.forEach(input => {
            const type = input.type.toLowerCase();
            const name = input.name.toLowerCase();
            const id = input.id.toLowerCase();

            if (type === 'text' || type === 'email' || type === 'tel' ||
                name.includes('user') || name.includes('login') || name.includes('email') ||
                id.includes('user') || id.includes('login') || id.includes('email')) {
                usernameInput = input;
            }

            if (type === 'password') {
                passwordInput = input;
            }
        });

        // 填充用户名
        if (usernameInput) {
            setInputValue(usernameInput, credential.username);
        }

        // 填充密码
        if (passwordInput) {
            setInputValue(passwordInput, credential.password);
        }

        // 关闭提示框
        removeExistingHints();

        // 显示成功消息
        showMessage('账号密码已填充', 'success');
    }

    // 设置输入框值
    function setInputValue(input, value) {
        // 触发React/Vue等框架的事件
        const nativeInputValueSetter = Object.getOwnPropertyDescriptor(
            window.HTMLInputElement.prototype, 'value'
        ).set;
        nativeInputValueSetter.call(input, value);

        // 触发各种事件
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
        input.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true }));
        input.dispatchEvent(new KeyboardEvent('keyup', { bubbles: true }));
        input.dispatchEvent(new KeyboardEvent('keypress', { bubbles: true }));
    }

    // 复制密码
    async function copyPassword(password) {
        try {
            await navigator.clipboard.writeText(password);
            showMessage('密码已复制到剪贴板', 'success');
        } catch (error) {
            console.error('复制失败:', error);
            showMessage('复制失败', 'error');
        }
    }

    // 显示消息
    function showMessage(text, type = 'info') {
        const message = document.createElement('div');
        message.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999999;
            padding: 12px 20px;
            border-radius: 8px;
            font-size: 14px;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            animation: slideIn 0.3s ease;
        `;

        if (type === 'success') {
            message.style.backgroundColor = '#f0f9eb';
            message.style.color = '#67c23a';
            message.style.border = '1px solid #e1f3d8';
        } else if (type === 'error') {
            message.style.backgroundColor = '#fef0f0';
            message.style.color = '#f56c6c';
            message.style.border = '1px solid #fde2e2';
        } else {
            message.style.backgroundColor = '#f4f4f5';
            message.style.color = '#909399';
            message.style.border = '1px solid #e9e9eb';
        }

        message.textContent = text;
        document.body.appendChild(message);

        // 添加动画样式
        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
        `;
        document.head.appendChild(style);

        // 3秒后移除
        setTimeout(() => {
            message.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => message.remove(), 300);
        }, 3000);
    }

    // 移除已存在的提示
    function removeExistingHints() {
        const existingHints = document.querySelectorAll(`.${CONFIG.HINT_CLASS_NAME}`);
        existingHints.forEach(hint => hint.remove());
    }

    // 监听页面变化
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.addedNodes.length) {
                // 检查是否有新的登录表单
                const loginForms = detectLoginForm();
                if (loginForms.length > 0) {
                    // 通知background script
                    chrome.runtime.sendMessage({
                        action: 'checkLoginPage',
                        url: window.location.href,
                        title: document.title
                    });
                }
            }
        });
    });

    // 开始观察
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });

    // 页面加载完成后检测
    window.addEventListener('load', () => {
        const loginForms = detectLoginForm();
        if (loginForms.length > 0) {
            chrome.runtime.sendMessage({
                action: 'checkLoginPage',
                url: window.location.href,
                title: document.title
            });
        }
    });
})();