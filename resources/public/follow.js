function heading_template(user_model) {
  var template = "<p class='home-link'><a href='/'>Home</a></p>";
  template += "<p><b>"+user_model.username+"</b> is following: ";
  user_model.follow.forEach(function(name) {
    template += name + ", ";
  });
  return template + "</center></p>";
}

function item_template(item, story) {
  var template = '<li><p class="comhead">';
  template += '<a href="https://news.ycombinator.com/user?id='+item.by+'">'+item.by+'</a>';
  template += ' ' + moment(item.time * 1000).fromNow() + ' ';
  template += '| <a href="https://news.ycombinator.com/item?id='+ item.id +'">Link</a> ';
  template += '| <a href="https://news.ycombinator.com/item?id='+ story.id +'">Root</a> ';

  template += "| <u><a href='" + story.url + "'>" + story.title + "</a></u>";

  if(item.text !== undefined) {
    template += '<div class="item">'+item.text+'</p>';
  }

  return template + '</li>';
}

function sort_clean_data(data) {
  var all_comments = [];

  // Flat map
  data.forEach(function(innteractions_list) {
    innteractions_list.forEach(function(pair) {
      all_comments.push(pair);
    });
  });

  // Sort
  all_comments.sort(function(a, b) {
    if( a[0].time > b[0].time ) return -1;
    else return 1;
  });

  return all_comments;
}

function prune_tree(data) {
  var tree = data.tree;

  tree.sort(function(a, b) {
    if( a.id > b.id ) return -1;
    else return 1;
  });

  var post = tree.filter(function(item) { return item.type == "story"; })[0];
  var comment = tree[0];

  return [comment, post];
}

function promise_for_usernames(names) {
  return names.map(function(name) {
    return $.getJSON("/i/" + name);
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

function register_form(username) {
  $('.form').show();
  $('.form .register input[name~=user]').val(username);
  $('.form .register').on('submit', function(e) {
    e.preventDefault();

    username = $('.form .register input[name~=user]').val();
    var follow = [];

    // Collect Users from form
    for(var i=1; i <= 10; ++i) {
      var a_user = $('.form .register input[name~=follow'+i+']').val();
      if(a_user != '') {
        follow.push(a_user);
      }
    }

    var request = { username: username, follow: follow };

    $.ajax({
      url: "/u",
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
        alert("error! " +  JSON.stringify(resp));
      }
    });
  });

}

function render_page(user_model) {

  var usernames = user_model.follow;
  var p_array = promise_for_usernames(usernames);

  $.when.apply($, p_array).done(function() {

    var args = Array.prototype.slice.call(arguments);
    var data = args.map(function(o) { return o[0]; });

    // Hack for a single promise returned
    if(p_array.length <= 1) {
      data = [ arguments[0] ];
    }

    var clean_data = data.map(function(da) {
      return da.interactions.map(prune_tree);
    });

    var all_interactions = sort_clean_data(clean_data);

    $('.heading').html(heading_template(user_model) );
    $('.comment-list').html('');

    // Each interaction pair
    all_interactions.forEach(function(pair) {
      var comment = pair[0];
      var post = pair[1];

      if( comment.deleted ) return;

      $('.comment-list').append( item_template( comment, post ) );
    });
  });
}

$(document).ready(function() {
  var username = get_param("user");

  if(username === undefined) {
    $('.comment-list').html('HN Follow');
  }

  $.getJSON("/a/" + username, function(model) {
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

    render_page(model);
  });
});