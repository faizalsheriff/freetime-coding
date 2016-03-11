<%@ taglib prefix="s" uri="/struts-tags" %>
 <div data-theme="c" data-role="header" data-position="fixed" >
          		<!--  <a href="#" onclick="javascript:loadhomepage();" data-iconpos="notext" data-icon="backs">Menu</a>
            <h1>Sign In</h1> -->
            
             <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadhomepage()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/barrow_icon.png" style="margin-top:2px;"></div>
            			</a>
            </div>	
           
          <div class="headerbartextcontainer">
		  <div class="headerbartext" style="font-size:1em;">Forgot Password</div></div>		
           
         </div>
         

         
	

   		<div data-role="content">
        <s:if test ="%{uiMessage!=null ||uiMessage!=''}" >
        <label style="display:block;color:#f62835;font-size: small; font-weight: bold; padding:10px"> 
        <s:property value="uiMessage"></s:property>
        </label>
        </s:if>
        
        	<label id="idforgotpassworderr" style="display:none"> </label>
        	<label for="idsemailid">
                            Please enter your Email, the password will be sent to the Email
                        </label>
        	<s:form id="idforgotpasswordform">   
            <input id="idsemailid" type="text" name="emailId" placeholder="Email Id" />
            
            </s:form>
            <a href="#" data-role="button" onclick="javascript:forgotpassword('#idforgotpasswordform')">Forgot Password</a>
           <!--  <p style="text-align:center;">or</p>
            <a href="loaduserenroll" data-role="button">Create an Account</a> -->
</div>