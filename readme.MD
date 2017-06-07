## 使用SVG实现自定义View map
1. 下载台湾地图的SVG 
2. 用[这个网站](http://inloop.github.io/svg2android/) 将svg资源转换成相应的 Android代码 
3. 利用Xml解析SVG的代码 封装成javaBean 最重要的得到Path 
4. 重写OnDraw方法 利用Path绘制台湾地图 
5. 重写OnTouchEvent方法，记录手指触摸位置，判断这个位置是否坐落在某个省份上
6. 使用 PathMeasure 与 ValueAnimator实现小蛇绕着城市跑的动画效果

<img src="file://readme_resource/taiwan.gif"/>