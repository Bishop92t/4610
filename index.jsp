<?xml version="1.0" encoding="UTF-8"?>
<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">
<html lang="en">
<head>

<meta name="google" content="notranslate">
<meta http-equiv="Content-Language" content="en">

	<style class="cp-pen-styles">
	@import url('https://fonts.googleapis.com/css?family=Amarante');

	html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, b, u, i, center, dl, dt, dd, ol, ul, li, fieldset, form, label, legend, table, caption, tbody, tfoot, thead, tr, th, td, article, aside, canvas, details, embed, figure, figcaption, footer, header, hgroup, menu, nav, output, ruby, section, summary, time, mark, audio, video {
	  	margin: 0;
  		padding: 0;
  		border: 0;
  		font-size: 100%;
  		font: inherit;
  		vertical-align: baseline;
  		outline: none;
  		-webkit-font-smoothing: antialiased;
  		-webkit-text-size-adjust: 100%;
  		-ms-text-size-adjust: 100%;
  		-webkit-box-sizing: border-box;
  		-moz-box-sizing: border-box;
  		box-sizing: border-box;
}
html { 
	overflow-y: scroll; 
}

body { 
  	background: #eee url('https://i.imgur.com/eeQeRmk.png'); /* https://subtlepatterns.com/weave/ */
  	font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  	font-size: 62.5%;
  	line-height: 1;
  	color: #585858;
  	padding: 22px 10px;
  	padding-bottom: 55px;
}

::selection { 
	background: #5f74a0; color: #fff; 
}

::-moz-selection { 
	background: #5f74a0; color: #fff; 
}

::-webkit-selection { 
	background: #5f74a0; color: #fff; 
}

br { 
	display: block; line-height: 1.6em; 
} 

article, aside, details, figcaption, figure, footer, header, hgroup, menu, nav, section { 
	display: block; 
}

ol, ul { 
	list-style: none;
}

input, textarea { 
  -webkit-font-smoothing: antialiased;
  -webkit-text-size-adjust: 100%;
  -ms-text-size-adjust: 100%;
  -webkit-box-sizing: border-box;
  -moz-box-sizing: border-box;
  box-sizing: border-box;
  outline: none; 
}

blockquote, q { 
	quotes: none; 
}

blockquote:before, blockquote:after, q:before, q:after { 
	content: ''; 
	content: none; 
}

strong, b { 
	font-weight: bold; 
} 

table { 
	margin: auto;
	border-collapse: collapse;
	border-spacing: 1; 
	border: 1px solid #EEEEEE;
}

td+td, th+th {
	border-left: 1px solid #EEEEEE;
}

tr+tr {
	border-top: 1px solid #EEEEEE;
}

img { 
	border: 0; max-width: 100%; 
}

h1 { 
  	font-family: 'Amarante', Tahoma, sans-serif;
  	font-weight: bold;
  	font-size: 3.6em;
  	line-height: 1.7em;
 	margin-bottom: 10px;
  	text-align: center;
}


/** page structure **/
#wrapper {
  	display: block;
  	width: 900px;
  	background: #fff;
  	margin: 0 auto;
  	padding: 7px 10px;
  	-webkit-box-shadow: 2px 2px 3px -1px rgba(0,0,0,0.35);
}

.bigtable {
	background: #ffffff;
}

.bigtextbox {
	height:69px;
	width:600px;
}

#keywords {
  	margin: 0 auto;
  	font-size: 1.2em;
  	margin-bottom: 15px;
}


#keywords thead {
	cursor: pointer;
 	background: #c9dff0;
}

#keywords thead tr th { 
	font-weight: bold;
	padding: 8px 10px;
	padding-left: 10px;
}

#keywords thead tr th span { 
	padding-right: 10px;
	background-repeat: no-repeat;
	background-position: 100% 100%;
}

#keywords thead tr th.headerSortUp, #keywords thead tr th.headerSortDown {
	background: #acc8dd;
}

#keywords thead tr th.headerSortUp span {
	background-image: url('https://i.imgur.com/SP99ZPJ.png');
}

#keywords thead tr th.headerSortDown span {
	background-image: url('https://i.imgur.com/RkA9MBo.png');
}


#keywords tbody tr { 
	color: #555;
}

#keywords tbody tr td {
	text-align: center;
	padding: 15px 10px;
}

#keywords tbody tr td.lalign {
	text-align: left;
}
</style>

</head>
<!-- credit to https://colorlib.com/wp/css3-table-templates/ for the CSS ideas -->

<body>
<script src='https://production-assets.codepen.io/assets/common/stopExecutionOnTimeout-b2a7b3fe212eaa732349046d8416e00a9dec26eb7fd347590fbced3ab38af52e.js'></script>
<script src='https://cdnjs.cloudflare.com/ajax/libs/jquery/2.1.3/jquery.min.js'></script>
<script src='https://cdnjs.cloudflare.com/ajax/libs/jquery.tablesorter/2.28.14/js/jquery.tablesorter.min.js'></script>
<script >$(function(){   $('#keywords').tablesorter();  }); //# sourceURL=pen.js  </script>

<!-- The big table that holds all of the pages content -->
<table><tr>
  <td class='bigtable'>

    <!-- The little table that holds the left frame -->
    <table>

	<tr><td><form action='/4610/Home' method='post'>
	<input type='hidden' name='name' value="+name+">
	<input type='image' src='http://52.26.169.0/pictures/logo.jpg' width=200 alt='Submit'>
	</form><br><br><br><br></td></tr>

	<tr><td><form action='/4610/IAAS' method='post'>
	<input type='hidden' name='name' value="+name+">
	<input type='image' src='http://52.26.169.0/pictures/iaas.jpg' width=200 alt='Submit'>
	</form><br><br></td></tr>

	<tr><td><form action='/4610/Storage' method='post'>
	<input type='image' src='http://52.26.169.0/pictures/storage.jpg' width=200 alt='Submit'>
	</form><br><br><br></td></tr>

    </table>
  </td>

  <!-- Space between left and right frame -->
  <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

  <td class='bigtable'>
    <div id='dynamic'>
	<p style="text-align:center;"><img src="http://52.26.169.0/pictures/logo.jpg"></p>

  <h2>Sign up, it's free!</h2>
	<form action="http://52.24.2.46:8080/4610/SignUp" method="post">
	  <table>
	    <tr><td><label>Name or Nickname:</label></td>
	    	<td><input name="name" type="text" id="Name or Nickname:"></td>
	    </tr>
	    <tr><td><label>Email Address:</label></td>
	    	<td><input name="email" type="email" id="Email Address:"></td>
	    </tr>
	    <tr><td><label>Desired Password:</label></td>
	    	<td><input name="password" type="password" id="Desired Password:"></td>
	    </tr>
	    <tr><td></td>
	    	<td id="right"><button type="submit">Sign Up</button></td>
	    </tr>
	  </table>
  	</form>
  
  <br><br><br><br><br><br>
  
	<div id="existing">
	  <form action="http://52.24.2.46:8080/4610/Login" method="post">
	  	<table>
	    	<tr><td>Existing User Name:</td>
	    		<td>Password:</td>
	    		<td></td>
	    	</tr>
	    	<tr><td><label><input name="name" type="text"></label></td>
	    		<td><label><input name="password" type="password"></label></td>
	    		<td><button type="submit">Login</button></td>
	    	</tr>
	    </table>	
	  </form>
	</div>


  </td></tr>
</table>

</body></html>