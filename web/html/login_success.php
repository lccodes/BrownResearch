<?php
session_start();
if(!session_is_registered(myusername)){
header("location:login.html");
}
?>

<html>
<head>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script>
window.onload=function(){
 (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
 (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
 m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
 })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
 ga('create', 'UA-51484089-1', 'jack.cs.brown.edu/login_success.php');
 //ga('set', '&uid', {{USER_ID}}); // Set the user ID using signed-in user_id.
 ga('send', 'pageview'); 
  
  alert("Whyyyyyyyyy");
}
</script>
</head>
<BODY onload="init()" style="overflow:hidden;height:100%;width:100%">
<h1>Your username is GLAMORADMIN</h1>
<TABLE align=left width=100% border="1" cellpadding="0" cellspacing="0"><TR><TD valign=top align=center WIDTH="1000" height="1000">
<iframe id="theframe" src="https://id2.s.nfl.com/fans/login" style="border:0px #FFFFFF none;" name="myiFrame" scrolling="no" frameborder="0" marginheight="0px" marginwidth="0px" height="100%" width="100%"></iframe>
</TD></TR></TABLE>

<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-51484089-1', 'auto');
  ga('send', 'pageview');

</script>
</BODY></html>
