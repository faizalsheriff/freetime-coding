<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
    <title>Home</title>
    <!--Mobile Tags-->
    <meta name="HandheldFriendly" content="True">
    <meta name="MobileOptimized" content="320">
    <meta name="viewport" content="width=device-width">
    <!--Load CND Hosted Resources-->
    <link rel="stylesheet" href="http://code.jquery.com/mobile/1.2.0/jquery.mobile-1.2.0.min.css" />
    <script src="http://code.jquery.com/jquery-1.8.2.min.js"></script>
    <script src="http://code.jquery.com/mobile/1.2.0/jquery.mobile-1.2.0.min.js"></script>
     <script src="javascript/ajax.js"></script>
      <script type="text/javascript">
    function shareFacebook(url,name,caption,description,img,redirurll)
	{
		var appid = "377002272362737";
		var redirecturi = redirurll;
		var uri = "https://www.facebook.com/dialog/feed?app_id="+appid+"&link="+encodeURIComponent(url)+"&redirect_uri="+encodeURIComponent(url)+"&caption="+encodeURIComponent(caption)+"&description="+encodeURIComponent(description)+"&name="+encodeURIComponent(name)+"&picture="+encodeURIComponent(img);
				newwindow=window.open(uri,'Share','status=0,toolbar=0,location=0,menubar=0,height=500,width=800,resizable=1');
				if (window.focus) {newwindow.focus()}
				return false;
	}
	function shareTwitter(url,text)
	{
		var uri = "http://www.twitter.com/share?url="+encodeURIComponent(url)+"&text="+encodeURIComponent(text)+"&counturl="+encodeURIComponent(url);
				newwindow=window.open(uri,'Share','status=0,toolbar=0,location=0,menubar=0,height=500,width=800,resizable=1');
				if (window.focus) {newwindow.focus()}
				return false;
	}
  </script>
    <link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>
<body>
    <section id="home" data-role="page" style="display:block;">
        <header data-role="header">
            <a href="javascript:loadnavigation()" data-rel="dialog" data-transition="reverse slide" data-iconpos="notext" data-icon="menu">Menu</a>
            <h1>Home</h1>
            <a href="#" data-iconpos="notext" data-icon="searchs">Search</a>
        </header>
        <div data-role="content">
       
			
		
			
			<span class="txt" style="position: absolute; text-decoration:none; top: 45px;left: 90px;z-index: 99;font-family: DINMedium; font-size: 20px;">My Winks is place for women and girls to Inspire, Represent, Share, and Lead our tomorrow  </span>
		
			
			
        </div>
    </section>
</body>
</html>
