<?php

// username and password sent from form 
$myusername=$_POST['myusername'] . ","; 
$mypassword=$_POST['mypassword']; 
$the_file = file_get_contents("theFile.txt");
//Check the login
if($myusername !== "," && strpos($the_file, $myusername) !== FALSE && substr($the_file, strpos($the_file, $myusername)+10, 9) == $mypassword){
// Register $myusername, $mypassword and redirect to file "login_success.php"
session_register("myusername");
session_register("mypassword"); 
header("location:login_success.php");
}
else {
echo "Wrong Username or Password";
header("location:login.html");
}
?>
