if(typeof String.prototype.trim !== 'function') {
  String.prototype.trim = function() {
    return this.replace(/^\s+|\s+$/g, ''); 
  }
}
// Theme specific functions
$(function(){
	//Add 'hasChildren' class to menu li's
	$('header nav ul li').each(function(i,item) {
		if ($(item).find('ul').length) {
			$(item).addClass('hasChildren');
		}
	});
	
	//Menu hover animation
	$("header nav ul li").each(function(i,item) {
		var list = $(this).find("> ul");
		$(this).hover(function() {
			list.stop('true','true').animate({
				opacity: 'toggle',
				paddingTop: 'toggle'
			});
		});
	});
	
	// Removes Sidebar area when no content is found
	var sb_con = $('#sidebarContainer');
	if (sb_con.text().trim().length === 0) {
		sb_con.remove();
	}
	
	var logo = $('#logo');
	if (logo.find('img').length === 0) {
		logo.remove();
	}
	// Check for sidebar plugin content
	if ($('#plugin_sidebar').text().length > 0) {
		// Add class to content so we can style it with a sidebar
		$('#content').addClass('hasPluginSidebarContent');
		
		// Prepend headers to blog sidebar content
		$.each(['blog-categories','blog-archives'], function() {
			$item = $('#' + this);
			if ($item.length) {
				$("<h3/>").text(this.substring(5)).prependTo($item);
			}
		});
		
		rss_feed_wrap = $('#blog-rss-feeds');
		if (rss_feed_wrap.length) {
			$('<h3/>').text('RSS Feeds').prependTo(rss_feed_wrap);
		}
		
		btc = $('.blog-tag-cloud');
		if (btc.length) {
			btc.before($("<h3/>").text('Tag Cloud'));
		}
	}
	
	// RMS Forms
	$('.form-input-field, div select:not(.fl-select)').each(function(i,el) {
		$(el).wrap('<div class="form-input-field-wrap"></div>');
	});
	
	$('form div label').each(function(i,el) {
		$(el).wrap('<div class="label-wrap"></div>');
	});
});