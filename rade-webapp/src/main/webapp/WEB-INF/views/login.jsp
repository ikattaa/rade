
<%
	/*
	 *  This file is part of the Rade project (https://github.com/mgimpel/rade).
	 *  Copyright (C) 2018 Marc Gimpel
	 *
	 *  This program is free software: you can redistribute it and/or modify
	 *  it under the terms of the GNU General Public License as published by
	 *  the Free Software Foundation, either version 3 of the License, or
	 *  (at your option) any later version.
	 *
	 *  This program is distributed in the hope that it will be useful,
	 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
	 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	 *  GNU General Public License for more details.
	 *
	 *  You should have received a copy of the GNU General Public License
	 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
	 */
%>
<%
	/* $Id$ */
%>
<%@page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec"
    uri="http://www.springframework.org/security/tags"%>
<jsp:include page="aesn_header.jsp" />
<div class="row justify-content-center">
    <div class="col-4">
        <c:if test="${param.error != null}">
            <div class="alert alert-danger">
                <spring:message code="login.error" />
            </div>
        </c:if>
        <c:if test="${param.logout != null}">
            <div class="alert alert-success">
                <spring:message code="login.logout" />
            </div>
        </c:if>
        <sec:authorize access="!isAuthenticated()">
            <div class="card card-aesn">
                <div class="card-body">
                    <form name="loginForm" action="/login" method="post">
                        <div class="form-group">
                            <div class="input-group">
                                <div
                                    class="input-group-prepend input-group-text btn-aesn">
                                    <i class="fas fa-user"
                                        aria-hidden="true"></i><span
                                        class="sr-only"><spring:message
                                            code="login.username" /></span>
                                </div>
                                <input type="text"
                                    class="form-control form-aesn"
                                    name="username"
                                    placeholder="<spring:message code="login.username.placeholder"/>"
                                    required="" autofocus="">
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="input-group input-group-md">
                                <div
                                    class="input-group-prepend input-group-text btn-aesn">
                                    <i class="fas fa-lock"
                                        aria-hidden="true"></i><span
                                        class="sr-only"><spring:message
                                            code="login.password" /></span>
                                </div>
                                <input type="password"
                                    class="form-control form-aesn"
                                    name="password"
                                    placeholder="<spring:message code="login.password.placeholder"/>"
                                    required="">
                            </div>
                        </div>
                        <button type="submit"
                            class="form-control btn-aesn" name="submit">
                            <i class="fas fa-sign-in-alt"
                                aria-hidden="true"></i>
                            <spring:message code="login.submit" />
                        </button>
                    </form>
                </div>
            </div>
        </sec:authorize>
    </div>
</div>
<jsp:include page="aesn_footer.jsp" />
