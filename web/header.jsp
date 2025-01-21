<header>
    <h1><span>ICP Tech Lab</span></h1>
    <ul class="system">
        <li class="first submenu"><span>Welcome <%= request.getSession().getAttribute("username") %></span>
            <ul>
                <li><a>Version 1.0.2</a></li>
                <li><a href="/ITL/user?requestType=viewUserProfile&userid=<%= request.getSession().getAttribute("userid")%>">View User Profile</a></li>
                <li onclick="window.location.href='login.jsp'">Logout</li>
            </ul>
        </li>
        <li><a href="help\ICP_Tech_Lab_ITL_User_Guide.pdf" target="_blank">Help</a></li>
    </ul>
    <ul class="primary">
        <li tabindex="0" class="first link" onclick="window.location.href='dashboard.jsp'"><span>Dashboard</span></li>
        <li tabindex="0" class="submenu left"><span>VM Services</span>
            <ul>
                <li tabindex="0"> <a href="/ITL/request?requestType=provisionVM">Provision VM Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=maintainVM">Maintain VM Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=deleteVM">Delete VM Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=viewRequests">View Requests</a></li>
            </ul>
        </li>
        <li tabindex="0" class="submenu left"><span>Product Services</span>
            <ul>
                <% String userGroup = (String) request.getSession().getAttribute("usergroup"); %>
                <li tabindex="0"> <a href="/ITL/request?requestType=ddrBaseInstall">6.0 - DDR Base Install Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=ddrUpgradeInstall">6.0 - DDR Upgrade from 5.4 ICP Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=ddrKbInstall">6.0 - DDR KB Install Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=baseInstall">5.4 - ICP Base Install Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=cuInstall">5.4 - ICP CU Install Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=kbLoad">5.4 - KB Load Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=lcdLoad">5.4 - LCD Load Request</a></li>
                <li tabindex="0"> <a href="/ITL/request?requestType=viewRequests">View Requests</a></li>
            </ul>
        </li>
        <li tabindex="0" class="submenu left"><span>Information</span>
            <ul>
                <li tabindex="0"> <a href="https://confluence.optum.com/display/Freya/ICP+Installation+Playbook" target="_blank">ICP Playbook</a></li>
                <li tabindex="0"> <a href="https://confluence.optum.com/display/Freya/ICP+Development" target="_blank">ICP Development</a></li>
                <li tabindex="0"> <a href="https://confluence.optum.com/display/Freya/ICP+Ops" target="_blank">ICP Ops</a></li>
                <li tabindex="0"> <a href="https://confluence.optum.com/display/IBSI" target="_blank">ICP Implementations</a></li>
            </ul>
        </li>
        <%if (userGroup.equalsIgnoreCase("devops")) { %>
        <li tabindex="0" class="submenu left"><span>Administration</span>
            <ul>
                <li tabindex="0"> <a href="/ITL/user?requestType=viewUserProfiles">View All User Profiles</a></li>
                <li tabindex="0"> <a href="/ITL/user?requestType=addUserProfile">Add User Profile</a></li>
                <li tabindex="0"> <a href="/ITL/user?requestType=viewUserVMs">View All User VMs</a></li>
                <li tabindex="0"> <a href="/ITL/user?requestType=addUserVM">Add User VM</a></li>
                <li tabindex="0"> <a href="/ITL/configProperty?requestType=updateConfigProperty">Update Config Property Table</a></li>
            </ul>
        </li>
        <%}%>
    </ul>
</header>
