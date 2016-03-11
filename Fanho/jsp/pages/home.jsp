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
    <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/img/fevi-icon-small.jpg"/>
    <link rel="stylesheet" href="https://www.mywinks.org/mywinks_sprt_files _new_look/1.2.0/jquery.mobile-1.2.0.min.css" />
    <script src="https://www.mywinks.org/mywinks_sprt_files _new_look/jquery-1.8.2.min.js"></script>
    <script src="https://www.mywinks.org/mywinks_sprt_files _new_look/1.2.0/jquery.mobile-1.2.0.min.js"></script>
    
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
<div id="home" data-role="page">
  
      
    <div id="homecontent" data-role="content">
		<h1 style="color: rgba(23,155,230,0.81); text-shadow: 0px 0px; font-family: DINMedium; font-size: 30px; line-height: 30px; padding: 0; margin: 8px 0px;">Welcome to My Winks</h1>
		<p style="color: rgb(255, 255, 255); text-shadow: 0px 0px; font-family: DINMedium; font-size: 17px; line-height: 20px; margin-top: 4px;font-weight:bold">Connects you with congress leaders, media and others to lead our tomorrow</p>
		
	<p style="margin-top:180px;">
            <a class="signinbtn" href="#" onclick="loadsigninpage()" style="" data-mini="true" data-inline="true"data-role="button">Sign In</a>
            <a class="signupbtn" href="#" onclick="loaduserenroll()" data-mini="true" data-inline="true" data-role="button" style="float:right;">Sign Up</a>
			</p>
     </div>
        
   
   <div id="ajaxpage" style="display:none"> </div>

  	   <div id="navigation" style="display:none;margin-top:0px;" class="ui-dialog-contain ui-corner-all ui-overlay-shadow">
        
        <div data-role="content" style="border-radius:0;" class="ui-corner-top ui-corner-bottom ui-content ui-body-c" >
            <ul data-role="listview" data-icon="none" data-iconpos="left" class="ui-listview">
               <!--  <li class="green" style="width:160px;" data-icon="false"><a  data-transition="slide" href="listmessages"><img class="ui-li-icon" src="img/today_icon.png" />Today</a></li>
                <li class="light_gray" style="width:135px;" data-icon="false"><a href="latest"><img class="ui-li-icon" src="img/latest_icon.png" />Latest</a></li>
                <li class="dark_gray" style="width:235px;" data-icon="false"><a href="#"><img class="ui-li-icon" src="img/saved_icon.png" />Saved for Later</a></li>
                <li class="miltry" style="width:180px;" data-icon="false"><a href="#"><img class="ui-li-icon" src="img/farrow_icon.png" />Design</a></li>
                <li class="sky_blue" style="width:190px;" data-icon="false"><a href="#"><img class="ui-li-icon" src="img/farrow_icon.png" />Friends</a></li>
                <li class="dark_purpel" style="width:200px;" data-icon="false"><a href="#"><img class="ui-li-icon" src="img/farrow_icon.png" />New York</a></li>
                <li class="sky_blue" style="width:190px;" data-icon="false"><a href="#"><img class="ui-li-icon" src="img/farrow_icon.png" />Friends</a></li>
            -->
           		
           		<li class="yellow" style="width:160px;" data-icon="false"><a href="#" onclick="hidenavigation();javascript:loadlistmessages()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Winks</a></li>
                <li class="light_gray" style="width:180px;" data-icon="false"><a href="#" onclick="hidenavigation();loadwhoismyrep()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Rep.</a></li>
                <li class="dark_purpel" style="width:235px;" data-icon="false"><a href="#" onclick="hidenavigation();loadrunningmadeeasy()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Campaign</a></li>
                <li class="dark_gray" style="width:235px;" data-icon="false"><a href="#" onclick="hidenavigation();loadyourbestadvocate()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Advocate</a></li>
                <li class="sky_blue" style="width:250px;" data-icon="false"><a href="#" onclick="hidenavigation();loadliststatistics()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Facts</a></li>
                <li class="miltry" style="width:250px;" data-icon="false"><a href="#" onclick="hidenavigation();loadrolemodels()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Models</a></li>
                <li class="green" style="width:205px;" data-icon="false"><a href="#" onclick="hidenavigation();loadtruthsucks()"><img class="ui-li-icon" src="img/farrow_icon.png" />Bitter Truth</a></li>
                <li class="miltry" style="width:190px;" data-icon="false"><a href="#" onclick="hidenavigation();loadwomenoutperformmen()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Motivation</a></li>
                <li class="light_gray" style="width:180px;" data-icon="false"><a href="#" onclick="hidenavigation();loadmytools()"><img class="ui-li-icon" src="img/farrow_icon.png" />My Tools</a></li>
           
            </ul>
            <a onclick="hidenavigation()" style="float:right;" href="#" data-corners="30px" data-iconshadow="false" data-role="button" data-icon="arrow-l" data-iconpos="notext" class="ui-btn ui-btn-up-c ui-shadow ui-btn-corner-all ui-btn-icon-notext" data-shadow="true" data-wrapperels="span" data-theme="c" title="ajax"><span class="ui-btn-inner ui-btn-corner-all"><span class="ui-btn-text">Ajax</span><span class="ui-icon ui-icon-arrow-l">&nbsp;</span></span></a>
        </div>
  	           
  
             
    </div>
    
</div>
    
</body>
</html>
