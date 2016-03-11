<%@ taglib prefix="s" uri="/struts-tags" %>
       
     <div data-theme="c" data-role="header" data-position="fixed" >
      <!--  <a href="#" onclick="loadnavigation()" data-rel="dialog" data-transition="reverse slide" data-iconpos="notext" data-icon="menu">Menu</a>
               
            <h3>My Facts</h3> -->
      	   <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="loadnavigation()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/latest_icon.png" style="margin-top:2px;"></div>
            			</a>
            </div>	
           
          <div class="headerbartextcontainer">
		  <div class="headerbartext">My Rep.</div>
		  </div>		
           
         
          
     </div>
           <div data-role="content" style="padding:2%;margin-top: 45px;">
        <h5>Representatives near me</h5>
     <!--    <h2>Reach out to your Rep. </h2> -->
        	<s:form id="idwhoismyrepform">
        		<input id="idzip" type="text" name="zipCode" placeholder="Enter Zip" />
            	
            </s:form>
            <a href="#" data-role="button" onclick="javascript:whoIsMyRep('#idwhoismyrepform')">Search Representative</a>
            <div id="repcontainer">
           
         
         </div>
        </div>
   
