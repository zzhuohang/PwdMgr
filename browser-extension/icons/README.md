# 图标说明

本目录需要放置以下尺寸的图标文件：

- `icon16.png` - 16x16 像素
- `icon48.png` - 48x48 像素
- `icon128.png` - 128x128 像素

## 图标设计建议

图标应该是一个锁的形状，代表密码管理：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128">
  <rect x="24" y="56" width="80" height="56" rx="8" fill="#409eff"/>
  <path d="M44 56V40a20 20 0 0 1 40 0v16" fill="none" stroke="#409eff" stroke-width="8" stroke-linecap="round"/>
  <circle cx="64" cy="80" r="8" fill="#fff"/>
  <rect x="60" y="80" width="8" height="16" rx="4" fill="#fff"/>
</svg>
```

## 生成图标

可以使用以下方法生成PNG图标：

1. 在线工具：https://convertio.co/svg-png/
2. 使用ImageMagick：
   ```bash
   convert icon.svg -resize 16x16 icon16.png
   convert icon.svg -resize 48x48 icon48.png
   convert icon.svg -resize 128x128 icon128.png
   ```

或者直接使用设计工具（如Figma、Sketch）创建图标。