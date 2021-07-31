# datetimepicker
一个非常好看的时间选择控件

[![](https://jitpack.io/v/Gredicer/datetimepicker.svg)](https://jitpack.io/#Gredicer/datetimepicker)

### 演示效果
![](demo.gif)

### 引用
Step 1. Add it in your root build.gradle at the end of repositories:
```kotlin
allprojects {
    repositories {
    ...
    maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency
```kotlin
dependencies {
    implementation 'com.github.Gredicer:datetimepicker:Tag'
}
```

### 使用

示例代码：

```
val dialog = DateTimePickerFragment.newInstance().mode(0).default("2010-10-10 11:11:11")
btn.setOnClickListener {
  dialog.show(this.supportFragmentManager, null)
}
dialog.listener = object : DateTimePickerFragment.OnClickListener {
	override fun onClickListener(selectTime: String) {
		Toast.makeText(applicationContext, selectTime, Toast.LENGTH_SHORT).show()
	}
}
```

其中 `mode`的值对应为：

```
0：默认，年月日时分
1：年选择
2：年月选择
3：年月日选择
4：时间选择
5：设定初始时间
```

`.default` 用来设置初始值，如果不设置默认为当前时间
