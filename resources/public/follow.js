function get_time_from_now(seconds) {
  var from_now;
  var current_time = new Date() / 1000;
  var seconds_since = current_time - seconds;

  if(seconds_since < 3600 ) {
    var minutes = Math.floor(seconds_since / 60);
    if(minutes === 1) {
      from_now = minutes + " minute ago"
    } else {
      from_now = minutes + " minutes ago";
    }
  } else if(seconds_since < 86400) {
    var hours = Math.floor(seconds_since / 3600);
    if(hours === 1) {
      from_now = hours + " hour ago";
    } else {
      from_now = hours + " hours ago";
    }
  } else if(seconds_since < 604800) {
    var days = Math.floor(seconds_since / 86400);
    if(days === 1) {
      from_now = days + " day ago";
    } else {
      from_now = days + " days ago";
    }
  } else if(seconds_since < 2678000) {
    var weeks = Math.floor(seconds_since / 604800);
    if(weeks === 1) {
      from_now = weeks + " week ago";
    } else {
      from_now = weeks + " weeks ago";
    }
  } else if(seconds_since < 31560000) {
    var months = Math.floor(seconds_since / 2678000);
    if(monts === 1) {
      from_now = months + " month ago";
    } else {
      from_now = months + " months ago";
    }
  } else {
    var years = Math.floor(seconds_since / 31560000);
    if(years === 1) {
      from_now = years + " year ago";
    } else {
      from_now = years + " years ago";
    }
  }

  return from_now;
}

function heading_template(user_model) {
  var template = "<p class='home-link'><a href='/'>Home</a></p>";
  template += "<p class='edit-link'><a href='/?user="+user_model.username+"&edit=true'>Edit</a></p>";
  template += "<p><b>"+user_model.username+"</b>'s follower list ";

  return template + "</center></p>";
}

function followers_tempalte(user_model) {
  var template = "<p class='comhead'>Following: ";

  for(var i = 0; i < user_model.follow.length; ++i) {
    var user = user_model.follow[i];
    template += "<a href='https://news.ycombinator.com/user?id="+user+"'>"+user+"</a>";

    if(i < (user_model.follow.length - 1)) {
      template += ", ";
    }
  }

 return template + "</p>";
}

function item_template(interaction) {
  var template = '<li><div class="item-body"><p class="comhead">';
  template += '<a href="https://news.ycombinator.com/user?id='+interaction.by+'">'+interaction.by+'</a>';
  template += ' ' + get_time_from_now(interaction.time) + ' ';
  template += '| <a href="https://news.ycombinator.com/item?id='+ interaction.id +'">Link</a> ';
  template += '| <a href="https://news.ycombinator.com/item?id='+ interaction.id +'">Root</a> ';

  template += "| <u><a href='" + interaction.url + "'>" + interaction.title + "</a></u>";

  if(interaction.text !== undefined) {
    template += '<div class="item">'+interaction.text+'</p>';
  }

  return template + '</div></li>';
}

function pagination_template(user, page) {
  var template = '<div class="pagination">';
  if(page > 1) {
    template += '<a href="/?user='+user+'&page='+(page - 1)+'">Previous</a>';
  }
  template += '<a href="/?user='+user+'&page='+(page + 1)+'">Next</a>';
  return template + '</div>';
}

function promise_for_usernames(names, page) {
  return names.map(function(name) {
    return $.getJSON("/api/u/" + name + "?page=" + page);
  });
}

function get_param(variable) {
  var query = window.location.search.substring(1);
  var vars = query.split("&");
  for (var i=0;i<vars.length;i++) {
    var pair = vars[i].split("=");
    if(pair[0] == variable){return pair[1];}
  }

  return undefined;
}

function register_form(username, followers, email) {
  $('.form').show();
  $('.form .register input[name~=user]').val(username);

  if(email !== null){
    $('.form .register input[name~=weekly_email]').prop('checked',true);
    $('.form .register input[name~=email]').val(email);
  }

  // Load followers
  if(followers !== undefined) {
    for(var i=0; i <= followers.length; ++i) {
      $('.form .register input[name~=follow'+(i+1)+']').val(followers[i]);
    }
  }

  $('.form .register').on('submit', function(e) {
    e.preventDefault();

    var username = $('.form .register input[name~=user]').val();
    var password = $('.form .register input[name~=password]').val();
    var follow = [];
    var email = null;
    if($('.form .register input[name~=weekly_email]').prop('checked')){
      email = $('.form .register input[name~=email]').val();
    }

    // Collect Users from form
    for(var i=1; i <= 10; ++i) {
      var a_user = $('.form .register input[name~=follow'+i+']').val();
      if(a_user != '') {
        follow.push(a_user);
      }
    }

    var request = { username: username,
                    password: password,
                    follow: follow,
                    email: email};

    if($("input[name~=new_password_check_box]").prop('checked')) {
      request.new_password = $("input[name~=new_password]").val();
    }

    $.ajax({
      url: "/api/u",
      method: "post",
      headers: {
        "Content-Type": "application/json"
      },
      data: JSON.stringify(request),
      dataType: 'json',
      error: alert
    }).success(function(resp) {
      if(resp.status == 'success') {
        // We're done registering the user, let's reload
        window.location = '/?user=' + username;
      } else {
        alert("Error! " +  resp.reason);
      }
    });
  });

}

function edit_view(username) {
  var template = "<p class='home-link'><a href='/?user="+username+"'>Back</a>";
  template += "</p><p>HN Follow - Edit followers</p>";
  $('.heading').html(template);
  $('.comments').hide();
  $('.form .goto').hide();
  $('.info').hide();
  $('.user-section').hide();
  $('.register input[type~=submit]').val('Update');
}

function render_page(user_model, page_number) {

  var usernames = user_model.follow;
  var p_array = promise_for_usernames(usernames, page_number);

  $.when.apply($, p_array).done(function() {

    var args = Array.prototype.slice.call(arguments);
    var data = args.map(function(o) { return o[0]; });

    // Hack for a single promise returned
    if(p_array.length <= 1) {
      data = [ arguments[0] ];
    }

    var all_user_lists = data.map(function(user_list) { return user_list.interactions; });
    var all_interactions = [].concat.apply([], all_user_lists);

    var sorted_interactions = all_interactions.sort(function(a, b) {
      if( a.time >= b.time ) return -1;
      else return 1;
    });

    $('.heading').html(heading_template(user_model) );
    $('.comments').prepend(followers_tempalte(user_model) );
    $('.comment-list').html('');
    $('.comment-list').attr('start', (page_number - 1) * (usernames.length * 5) + 1);

    // Each interaction pair
    sorted_interactions.forEach(function(interaction) {

      $('.comment-list').append( item_template( interaction ) );
    });

    // Render Pagination

  });
}

$(document).ready(function() {
  var username = get_param("user");

  $('.new-password-section').hide();

  if(username === undefined || username == '') {
    $('.heading').html('<p>HN Follow</p>');
    $('.comments').hide();
    register_form(username);
    return;
  }

  $.getJSON("/api/a/" + username, function(model) {
    if(model.follow.length === 0) {
      $('.comments').hide();
      if(username === undefined) {
        $('.heading').html('<p>HN Follow</p>');
      } else {
        $('.heading').html('<p>No such user</p>');
      }

      register_form(username);
      return;
    }

    if(get_param("edit") == "true") {
      edit_view(username);
      register_form(username, model.follow, model.email);
      $('.new-password-section').show();
      $('p.subheader').hide();
      return;
    }

    var page_number = parseInt(get_param("page"));
    if(isNaN(page_number)) {
      page_number = 1;
    }

    render_page(model, page_number);

    $('.comments').append(pagination_template(username, page_number));
  });
});
