<%@ taglib prefix="s" uri="/struts-tags" %>
<%-- <!DOCTYPE html>
<html>
<head>
    <title>Navigation</title>
    <!--Mobile Tags-->
    <meta name="HandheldFriendly" content="True">
    <meta name="MobileOptimized" content="320">
    <meta name="viewport" content="width=device-width">
    <!--Load CND Hosted Resources-->
    <link rel="stylesheet" href="http://code.jquery.com/mobile/1.2.0/jquery.mobile-1.2.0.min.css" />
    <script src="http://code.jquery.com/jquery-1.8.2.min.js"></script>
    <script src="http://code.jquery.com/mobile/1.2.0/jquery.mobile-1.2.0.min.js"></script>
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
     <script src="javascript/ajax.js"></script>
     
    <link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>
<body>
    <section id="navigation" data-role="page"> 
        
        <div data-role="content" style="border-radius:0;">--%>
            <ul data-role="listview" data-icon="none" data-iconpos="left">
           		<li class="yellow" style="width:160px;" data-icon="false"><a href="#" onclick="javascript:loadlistmessages()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Winks</a></li>
                <li class="light_gray" style="width:180px;" data-icon="false"><a href="loadwhoismyrep"><img class="ui-li-icon" src="img/farrow_icon.png" />My Rep.</a></li>
                <li class="dark_purpel" style="width:235px;" data-icon="false"><a href="runningmadeeasy"><img class="ui-li-icon" src="img/farrow_icon.png" />My Campaign</a></li>
                <li class="dark_gray" style="width:235px;" data-icon="false"><a href="yourbestadvocate"><img class="ui-li-icon" src="img/farrow_icon.png" />My Advocate</a></li>
                <li class="sky_blue" style="width:250px;" data-icon="false"><a href="loadliststatistics"><img class="ui-li-icon" src="img/farrow_icon.png" />My Facts</a></li>
                <li class="miltry" style="width:250px;" data-icon="false"><a href="loadrolemodels"><img class="ui-li-icon" src="img/farrow_icon.png" />My Models</a></li>
                <li class="green" style="width:205px;" data-icon="false"><a href="truthsucks"><img class="ui-li-icon" src="img/farrow_icon.png" />Bitter Truth</a></li>
                <li class="miltry" style="width:190px;" data-icon="false"><a href="womenoutperformmen"><img class="ui-li-icon" src="img/farrow_icon.png" />My Motivation</a></li>
                <li class="light_gray" style="width:180px;" data-icon="false"><a href="loadmytools"><img class="ui-li-icon" src="img/farrow_icon.png" />My Tools</a></li>
            </ul>
           <a style="float:right;" href="#" data-rel="back" data-corners="30px" data-iconshadow="false" data-role="button" data-icon="arrow-l" data-iconpos="notext" class="back_btn" data-transition="slide">Back</a>
<%--        </div>
 
    </section>
</body>
</html> --%>
