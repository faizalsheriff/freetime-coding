<%@ taglib prefix="s" uri="/struts-tags" %>
       
       <div data-theme="c" data-role="header" data-position="fixed" >
          <!--   <a href="javascript:loadlistcompaigns()" data-transition="reverse slide" data-iconpos="left" data-icon="delete">Cancel</a>
            <h1>Create Event</h1>
            <a href="#" data-iconpos="right" data-icon="arrow-r" onclick="javascript:addCompaign('#addcompaignform')">Post</a> -->
            
            <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadlistcompaigns()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:58px;" class="menuicon ui-btn-corner-all"><div class="position_text">Cancel</div></div>
            			</a>
            	</div>	
            <div class="menuicon right">	
            		 <a href="#" onclick="javascript:addCompaign('#addcompaignform')" class="adjust_top">
            		 	<div style="display:inline;float:right;margin:7px 3px;height:32px;width:52px;" class="menuicon ui-btn-corner-all"><div class="position_text">Post</div></div>
            		 </a>
            </div>
        </div>
        <div data-role="content" style="height:100%; padding:0px;">
         	<s:form id="addcompaignform">
                    <s:hidden id="idsid" name = "sid" value="1"></s:hidden>
                    <s:hidden name = "compaignId" value="%{compaignId}"></s:hidden>
                    <s:hidden id="idcompaigncontents" name = "compaignContents" value=""></s:hidden>
                    <s:hidden id="idlastupdatetime" name = "lastUpdateTimeString" value=""></s:hidden>
                    
            </s:form> 
            
            <textarea id="txtStatus" placeholder="Run virtual campaign inviting your local US congress representative, journalists and your friends at other social media - gather more 'Votes up' to seek attention" class="txtareapost" rows="6"  name="txtStatus" ></textarea>
            <img class="txtpic" src="img/pic_icon.jpg" />
        </div>
   	    <div data-theme="c" data-role="footer" data-position="fixed" >
           <!-- <input type="file" style="width: 30px; opacity: 0;" /><div style="margin-left:48%;margin-bottom:17px;"><a style="z-index:-1;"  data-role="button" data-iconpos="notext" data-icon="camera" >Add Image</a>--></div>
        </div>
 