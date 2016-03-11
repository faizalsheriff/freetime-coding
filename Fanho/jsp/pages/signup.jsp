<%@ taglib prefix="s" uri="/struts-tags" %>
        <div data-theme="c" data-role="header" data-position="fixed" >
      <!--   <a href="#" onclick="javascript:loadhomepage();" data-iconpos="notext" data-icon="backs">Menu</a>
         <h1>Sign Up</h1> 
      -->
       <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadhomepage()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/barrow_icon.png" style="margin-top:2px;"></div>
            			</a>
            </div>	
         
        <!--   <a href="#" data-iconpos="notext" data-icon="searchs">Search</a> -->
         <div style="display-inline;">
		  <div class="headerbartext">Sign Up </div></div>		
           
         </div>
        </div>
        <div data-role="content">
        <s:if test="%{errorMessage!='' || errorMessage!=null}">
        <label style="display:block;color:#f62835;font-size: small; font-weight: bold; padding:10px"> 
        <s:property value="errorMessage"></s:property></label>
        </s:if>
          <s:form id="idenrolluserform">
            <input type="text" name="userName" placeholder="User Name" />
            <input type="email" name="emailID" placeholder="Email" />
			<input id="idpassword" name="password" type="password" placeholder="Password" />
            <input id="idconfirmpassword" name="confirmpassword" type="password" placeholder="Confirm Password" />
		   </s:form>
	
            
         	<label id="idpwderr" style="display:none">                     
            </label>
            <a href="#" data-role="button" onclick="javascript:enrollUser('#idenrolluserform')">Create an Account</a>
            
            
        </div>
   
