<%@ taglib prefix="s" uri="/struts-tags" %>

        <div data-theme="c" data-role="header" data-position="fixed" >
           <!--  <a href="#"  onclick="javascript:loadlistmessages()" data-transition="reverse slide" data-iconpos="left" data-icon="delete">Cancel</a>
            <h1>Update Status</h1>
            <a href="#" data-iconpos="right" data-icon="arrow-r" onclick="javascript:addMessage('#addmessageform')">Post</a> -->
            	<div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadlistmessages()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:58px;" class="menuicon ui-btn-corner-all"><div class="position_text">Cancel</div></div>
            			</a>
            	</div>	
            	<div class="menuicon right">	
            		 <a href="#" onclick="javascript:addMessage('#addmessageform')" class="adjust_top">
            		 	<div style="display:inline;float:right;margin:7px 3px;height:32px;width:52px;" class="menuicon ui-btn-corner-all"><div class="position_text">Post</div></div>
            		 </a>
            	</div>
            
        </div>
        <div id="idaddmessagecontentsdiv" data-role="content" style="height:100%; padding:0px;">
           <s:form id="addmessageform">
                    <s:hidden id="idsid" name = "sid" value="1"></s:hidden>
                    <s:hidden name = "messageId" value="%{messageId}"></s:hidden>
                    <s:hidden id="idmessagecontents" name = "messageContents" value=""></s:hidden>
                    <s:hidden id="idlastupdatetime" name = "lastUpdateTimeString" value=""></s:hidden>
                    
            </s:form> 
         
            <textarea id="txtStatus" placeholder="Post local issues - seek attention, Share your community achievements - inspire others, Post seeking help to run for office etc..."  rows="6" class="txtareapost" name="txtStatus" ></textarea>
            <img class="txtpic" src="img/pic_icon.jpg" />
            
            
        </div>
      		 <div data-theme="a" data-role="footer" data-position="fixed" >
            <!-- <div style="margin-left: 48%;margin-bottom: 17px; "><input type="file" id="the-file" onchange="handleMessagePictures(this.files)" style="width: 30px; position: absolute; opacity: 0;" /><a style="z-index:-1;"  data-role="button" data-iconpos="notext" data-icon="camera">Add Image</a></div> -->
              <div style="margin-left: 48%;margin-bottom: 17px; ">
              <input type="file" id="the-file" onchange="handleMessagePictures(this.files)" style="width: 30px; position: absolute; opacity: 0; display:hidden;" />
            <div>
              <a >
              <div>
					<div class="camera_conatiner">
			           <img style="line-height:-20px;display:block;margin:5px;" src="img/cameranew_icon.png">
					</div>
              </div>
              </a>
             </div>
              </div>
      		</div>
   