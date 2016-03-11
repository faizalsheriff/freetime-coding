<%@ taglib prefix="s" uri="/struts-tags" %>
          <div data-theme="c" data-role="header" data-position="fixed" >
          		   <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadhomepage()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/barrow_icon.png" style="margin-top:2px;"></div>
            			</a>
            	   </div>	
            	    <div style="display-inline;">
		  						<div class="headerbartext">Sign In </div></div>		
           
         				</div>
            <h1>Sign In</h1>
         </div>
         
	

   		<div data-role="content">
        <s:if test ="%{uiMessage!=null ||uiMessage!=''}" >
        <label style="display:block;color:#f62835;font-size: small; font-weight: bold; padding:10px"> 
        <s:property value="uiMessage"></s:property>
        </label>
        </s:if>
        
        	<label id="idloginpwderr" style="display:none"> </label>
        	<s:form id="idsigninform">   
            <input id="idsusername" type="text" name="username" placeholder="User Name" />
            <input id="idspwd" type="password" name="password" placeholder="Password" />
            </s:form>
            <a href="#" data-role="button" style="background-image: -moz-linear-gradient(top, #faeeae 0, #faeeae 1px, #f5db59 1px, #f6c408 100%);background-image: -o-linear-gradient(top, #faeeae 0, #faeeae 1px, #f5db59 1px, #f6c408 100%);background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #faeeae), color-stop(5%, #faeeae), color-stop(5%, #f5db59), color-stop(100%, #f6c408));background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #faeeae), color-stop(5%, #faeeae), color-stop(5%, #f5db59), color-stop(100%, #f6c408));background-image: linear-gradient(top, #faeeae 0%,#faeeae 1px,#f5db59 1px,#f6c408 100%);" onclick="javascript:signin('#idsigninform')">Sign In</a>
           <!--  <p style="text-align:center;">or</p>
            <a href="loaduserenroll" data-role="button">Create an Account</a> -->
        </div>
            