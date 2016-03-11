      <%@ taglib prefix="s" uri="/struts-tags" %>
        <div data-theme="c" data-role="header" data-position="fixed" >
          <!--     <a href="#" onclick="javascript:loadhomepage();" data-transition="reverse slide" data-iconpos="notext" data-icon="menu">Home</a>
            <h1>You</h1>
            <a href="#" data-iconpos="notext" data-icon="searchs">Search</a> -->
            
             <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadhomepage()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/barrow_icon.png" style="margin-top:2px;"></div>
            			</a>
            </div>	
           
          <div class="headerbartextcontainer">
		  <div class="headerbartext">You</div></div>		
           
         </div>
         
	
	
       
        <div data-role="content">
    
        <s:form  id="idaddaboutmeform">
       	<s:hidden name="userName" value="%{viewUser.userName}"></s:hidden>
		<s:hidden name="userId" value="%{viewUser.userId}"></s:hidden>
        </s:form>
        <div class="me">
		<div class="info">
	
		
		<h1 class="name">Welcome <s:property value="viewUser.userName"></s:property></h1>
		
		 <img class="pic" src="img/pic_icon.png"/ style="margin-top: 15px">
		
		</div>
		
		<div class="info_box">
		<p class="hd" style="height:7em;margin-top:2em;">
		 <label>What are the top two actions that you will take as a president of this country </label>
		 <textarea name="slogan" maxlength="550" >
		 </textarea>
		</p>
		<p class="hd" style="height:7em;margin-top:2em;">
		  <label>Your Strengths and Interests</label>
		  <textarea name="aboutMe" placeholder="*Comma separated" maxlength="550">
		 </textarea>
		</p>
	<!-- 	<p>Something more about me.</p> -->
		
		
	
		 <p class="hd" style="height:7em;margin-top:2em;">  
		 <label>Your Role Model</label>
		  <textarea name="myIntrests" placeholder="Your intrests and hobbies" maxlength="550">
		 </textarea>
		 </p>
		   <a href="#" data-role="button" onclick="javascript:addAboutMe('#idaddaboutmeform')">Add</a>
		   <a href="#" onclick="javascript:loadlistmessages()" data-role="button" >Skip</a>
		</div>
		 
		</div>
		
		 
		</div>
    