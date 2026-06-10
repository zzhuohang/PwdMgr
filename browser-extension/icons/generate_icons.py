"""生成浏览器扩展图标"""
from PIL import Image, ImageDraw, ImageFont
import os

def create_lock_icon(size):
    """创建锁形状的图标"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 计算比例
    scale = size / 128.0

    # 背景圆角矩形（锁体）
    body_x1 = int(24 * scale)
    body_y1 = int(56 * scale)
    body_x2 = int(104 * scale)
    body_y2 = int(112 * scale)
    radius = int(8 * scale)

    # 绘制渐变效果的锁体
    for i in range(body_y1, body_y2):
        progress = (i - body_y1) / (body_y2 - body_y1)
        r = int(64 + (51 - 64) * progress)
        g = int(158 + (126 - 158) * progress)
        b = int(255 + (204 - 255) * progress)
        draw.line([(body_x1, i), (body_x2, i)], fill=(r, g, b, 255))

    # 锁扣（拱形）
    arch_x1 = int(44 * scale)
    arch_y1 = int(24 * scale)
    arch_x2 = int(84 * scale)
    arch_y2 = int(56 * scale)
    line_width = max(int(8 * scale), 2)

    # 绘制锁扣
    draw.arc(
        [arch_x1, arch_y1, arch_x2, arch_y2],
        180, 0,
        fill=(64, 158, 255, 255),
        width=line_width
    )
    # 锁扣两侧竖线
    draw.line(
        [(arch_x1, int(40 * scale)), (arch_x1, int(56 * scale))],
        fill=(64, 158, 255, 255),
        width=line_width
    )
    draw.line(
        [(arch_x2, int(40 * scale)), (arch_x2, int(56 * scale))],
        fill=(64, 158, 255, 255),
        width=line_width
    )

    # 锁孔圆点
    cx = int(64 * scale)
    cy = int(80 * scale)
    cr = int(8 * scale)
    draw.ellipse(
        [cx - cr, cy - cr, cx + cr, cy + cr],
        fill=(255, 255, 255, 255)
    )

    # 锁孔竖条
    slot_x1 = int(60 * scale)
    slot_y1 = int(80 * scale)
    slot_x2 = int(68 * scale)
    slot_y2 = int(96 * scale)
    slot_r = int(4 * scale)
    draw.rounded_rectangle(
        [slot_x1, slot_y1, slot_x2, slot_y2],
        radius=slot_r,
        fill=(255, 255, 255, 255)
    )

    return img

def main():
    icon_dir = os.path.dirname(os.path.abspath(__file__))
    sizes = [16, 48, 128]

    for size in sizes:
        img = create_lock_icon(size)
        filename = f"icon{size}.png"
        filepath = os.path.join(icon_dir, filename)
        img.save(filepath, "PNG")
        print(f"Generated: {filename}")

if __name__ == "__main__":
    main()
