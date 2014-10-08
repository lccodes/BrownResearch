// Theme specific functions
$(document).ready(function() {
	var sb_con = $('#sidebarContainer');
	if (sb_con.text().trim().length === 0) {
		sb_con.remove();
	}
	
	sb_con.remove().insertAfter('#main');
});