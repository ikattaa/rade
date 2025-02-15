<%/*
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
 */%>
<%/* $Id$ */%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="../aesn_header.jsp" />
<div class="row justify-content-center">
	<div class="col-12">
		<div class="card card-aesn">
			<div class="card-body">
				<table class="w-100">
					<tr>
						<td class="w-16"><b>Code INSEE:</b></td>
						<td><input type="text" class="w-33" value="${region.codeInsee}" readonly/></td>
					</tr>
					<tr>
						<td><b>Nom Enrichi:</b></td>
						<td><input type="text" class="w-33" value="${region.nomEnrichi}" readonly/></td>
					</tr>
					<tr>
						<td><b>Nom Majuscule:</b></td>
						<td><input type="text" class="w-33" value="${region.nomMajuscule}" readonly/></td>
					</tr>
					<tr>
						<td><b>Début Validité:</b></td>
						<td><input type="text" class="w-33" value="${region.debutValidite}" readonly/></td>
					</tr>
					<tr>
						<td><b>Fin Validité:</b></td>
						<td><input type="text" class="w-33" value="${region.finValidite != null ? region.finValidite : ''}" readonly/></td>
					</tr>
				</table>
			</div>
		</div>
	</div>
</div>
<jsp:include page="../aesn_footer.jsp" />
