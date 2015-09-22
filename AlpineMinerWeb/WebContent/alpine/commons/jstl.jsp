<%
 String language = request.getLocale().getLanguage();
%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="<%=language%>" />
