# LineViewDemo
### An Android LineView Demo
- Notice : addAndFormat() should not used in onCreate() directly. Because it prior to onSizeChanged() execute which cannot get the View's high and width.

### 动态绘制折线图的Demo

- 注意一件事：addAndFormat()函数直接在onCreate里调用的话，他先于onSizeChanged()函数执行，就得不到控件的高度，绘制会出现问题
