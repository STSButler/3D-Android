# 3D-Android
<h1>Android: 3D Views</h1>
<p>
This Android app displays 3D scenes. Tap a button in the upper right corner, to change scenes. Swipe to view. The project was implemented with Android Studio 2020, Java, OpenGL ES, the NDK and XML. The project currently includes Java, res and gradle files.
</p>
<h2>Efficiency</h2>
<p>
This app does not use sky boxes with multiple textures. Load <em>one cube mesh</em> for all scenes. Load <em>one texture map</em>, rather than six, for each scene. Additionally each scene includes room to texture other sprites or meshes.
</p>
<p>
This application was ported from WebGL projects prepared for the book, "3D Scenes: Learn WebGL Book 3". The book includes explanation, JavaScript, WebGL source code and graphics downloads. 
</p>
<p>
You may download the free, compiled 3D Views app and read the code, with explanation, at
<a href="https://android.7thunders.biz/apps/3d.php" title="Android 3D Apps">Android 3D Apps</a> and
<a href="https://android.7thunders.biz/code/views.php" title="3D Views: Source Code">3D Views: Source Code</a>.
</p>
<h3>NDK</h3>
<p>
This project uses NDK version 21.4.7075529 which allows compatibility with older Androids.
Modify the location of the NDK for your configuration.
</p>
