/*
 * Scout449 Web Interface
 * AJAX version 2.0
 *
 * The old web interface has been removed to reduce code maintenance, but it can be added
 *  back later if it's *really* needed. I don't want to do this, but if you must, contact me.
 */

var plusImg, minusImg, loadImg, stopImg;
var defaultShow = 25, defaultTeams = 16;
var nextMatchNum = 1, lastWhen = 0, lastLabel = 0, lastTime = false;
var validBrowser = false, isIE7 = false;
var offset = 0, late = 0, times = 0, rate = 0;
var tUdfs = new Array(), comment = "";
var ua = navigator.userAgent;
var req = false;
var old = -1;
var active = false;
if (document.getElementById && (req = createRequest())) {
 validBrowser = true;
 if (ua.indexOf("MSIE") > 0) { // IE, check for 7
  var ind = ua.indexOf("MSIE");
  ind = parseFloat(ua.substring(ind + 5, ind + 8));
  if (ind >= 7 && ind < 8) isIE7 = true;
 }
}

function preload() {
 if (document.images) {
  plusImg = new Image();
  plusImg.src = 'imgrs?plus.gif';
  minusImg = new Image();
  minusImg.src = 'imgrs?minus.gif';
  stopImg = new Image();
  stopImg.src = 'imgrs?nloading.gif';
  loadImg = new Image();
  loadImg.src = 'imgrs?loading.gif';
 }
}

var ie7;
var ie7index = 0;

if (isIE7) ie7 = new Array();

function ie7fix(id) {
 // BUG in IE7 - links are broken wihtout a fix
 if (isIE7) ie7[ie7index++] = id;
}

function ie7done() {
 if (isIE7) {
  for (var i = 0; i < ie7index; i++) {
   document.getElementById(ie7[i]).style.cursor = 'hand';
   document.getElementById(ie7[i]).removeAttribute('href');
  }
  ie7 = new Array();
  ie7index = 0;
 }
}

function getDate() {
 var now = new Date();
 now.setTime(now.getTime() + offset);
 return now;
}

function setTime() {
 var now = getDate();
 var str = '<font color="#FFFFFF">' + format(now) + '<\/font><br />';
 if (late < 0)
  str += '<font color="#CCFFCC">' + (-late) + ' min early<\/font>';
 else if (late > 0)
  str += '<font color="#FFCCCC">' + late + ' min late<\/font>';
 else
  str += '<font color="#FFFFFF">on time<\/font>';
 document.getElementById('btime').innerHTML = str;
 window.setTimeout('setTime()', 2000);
 if (times > 30) {
  times = 0;
  sTime();
 } else times++;
 return true;
}

function createRequest(){
 var request = false;
 if (window.XMLHttpRequest) {
  // DOM, Gecko-based, Safari...
  request = new XMLHttpRequest();
 } else if (window.ActiveXObject){
  // IE, try 2 versions
  try {
   request = new ActiveXObject("Msxml2.XMLHTTP");
  } catch (ex) {
   try {
    request = new ActiveXObject("Microsoft.XMLHTTP");
   } catch (ex) {
    alert('No ActiveX object is available for AJAX.');
   }
  }
 }
 return request;
}

function load() {
 if (document.images && loadImg)
  document.getElementById('loading').src = loadImg.src;
 else
  document.getElementById('loading').src = 'imgrs?loading.gif';
 return false;
}

function stopLoad() {
if (document.images && stopImg)
  document.getElementById('loading').src = stopImg.src;
 else
  document.getElementById('loading').src = 'imgrs?nloading.gif';
 return false;
}

function clearRight() {
 setTitle(-1);
 return setRight("");
}

var open = new Array();

function createModule(title, id, text) {
 var out = '<a href="#" onclick="return oc(\'' + id + '\')" class="amod" onmouseover="return setStatus(\'Open/Close\', \'a_';
 out += id + '\')" onmouseout="return ';
 out += '"clrStatus(\'a_' + id + '\')" id="a_' + id + '"><img border="0" alt="" id="i_' + id + '" src="imgrs?plus.gif"';
 out += " />\n<b>" + title + '<\/b><\/a><div id="d_' + id + '" style="display: none" class="dmod">' + text + "<\/div>\n";
 open[id] = false;
 ie7fix(id);
 return out;
}

function oc(id) {
 load();
 if (open[id]) {
  if (document.images && plusImg)
   document.getElementById('i_' + id).src = plusImg.src;
  else
   document.getElementById('i_' + id).src = 'imgrs?plus.gif';
  document.getElementById('d_' + id).style.display = 'none';
 } else {
  if (document.images && minusImg)
   document.getElementById('i_' + id).src = minusImg.src;
  else
   document.getElementById('i_' + id).src = 'imgrs?minus.gif';
  document.getElementById('d_' + id).style.display = 'block';
 }
 open[id] = !open[id];
 return stopLoad();
}

function viewTeamList(teams, start, len) {
 var end = start + len, inc = len;
 var out = '<table border="0" cellpadding="0" cellspacing="0" id="tlist">';
 var count = 0, team;
 len = 0;
 for (teamNum in teams) {
  if (len < start || len >= end) {
   len++;
   continue;
  }
  team = teams[teamNum];
  if (count % 4 == 0) out += '<tr>';
  out += '<td class="tcell" align="center" valign="middle" id="ttd' + team.number + '" width=\"140\"><a onclick="return ';
  out += 'showTeam(' + team.number + ')" href="#" onmouseover="return setTHL(' + team.number + ')" onmouseout="return ';
  out += 'clearTHL(' + team.number + ')" id="teamlist_' + team.number + '">';
  out += '<img src="thumbnail?team=' + team.number + '" border="0" alt="No Image" />';
  out += '<br />' + team.name + '<br />' + team.number + "<\/a><\/td>\n";
  ie7fix('teamlist_' + team.number);
  if (count % 4 == 3) out += '<\/tr>';
  count++; len++;
 }
 var o2;
 if (len / inc >= 1)
  o2 = "Go To Page:\n";
 else
  o2 = "\n";
 var i; active = teams;
 for (i = 0; i < start; i += inc) {
  o2 += '<a href="#" onclick="return viewTeamList(active, ' + i + ', ' + inc + ')" onmouseover="return setStatus(\'Go To';
  o2 += ' Page\', \'goto_' + i + '\')" onmouseout="return clrStatus(\'goto_' + i + '\')" id="goto_' + i + '">';
  o2 += (i / inc + 1) + "<\/a>\n";
  ie7fix('goto_' + i);
 }
 if (len / inc >= 1) o2 += '<b>' + (i / inc + 1) + "<\/b>\n";
 i += inc;
 for (; i < len; i += inc) {
  o2 += '<a href="#" onclick="return viewTeamList(active, ' + i + ', ' + inc + ')" onmouseover="return setStatus(\'Go To';
  o2 += ' Page\', \'goto_' + i + '\')" onmouseout="return clrStatus(\'goto_' + i + '\')" id="goto_' + i + '">';
  o2 += (i / inc + 1) + "<\/a>\n";
  ie7fix('goto_' + i);
 }
 if (count % 5 != 0) {
  for (i = count % 4; i < 4; i++)
   out += '<td>&nbsp;<\/td>';
  out += '<\/tr>';
 }
 if (count == 0)
  out = 'Showing <b>0<\/b> of <b>0<\/b> teams.' + out;
 else if (start + 1 == Math.min(end, len))
  out = 'Showing <b>' + (start + 1) + '<\/b> of <b>' + len + '<\/b> team' + (len == 1 ? '' : 's') + '. &nbsp;' + 
   '<form action="#" onsubmit="return showTeam(this.team.value);"><b>View:<\/b> <input type="text"' +
   ' name="team" id="team" size="4" maxlength="5" /> <input type="submit" value="Look Up" /></form><br />' + o2 + out;
 else
  out = 'Showing <b>' + (start + 1) + '-' + Math.min(end, len) + '<\/b> of <b>' + len + '<\/b> teams. &nbsp;' + 
   '<form action="#" onsubmit="return showTeam(this.team.value);"><b>View:<\/b> <input type="text"' +
   ' name="team" id="team" size="4" maxlength="5" /> <input type="submit" value="Look Up" /></form><br />' + o2 + out;
 out += '<\/table>' + o2;
 setRight(out);
 if (count != 0) document.getElementById('team').focus();
 return false;
}

function showFavList() {
 load();
 var ateams = new Array(), team;
 for (var i = 0; i < favTeams.length; i++) {
  team = favTeams[i];
  if (!team || team <= 0) continue;
  ateams[team] = teams[team];
 }
 setTitle(4);
 viewTeamList(ateams, 0, defaultShow);
 return stopLoad();
}

function showTeamList() {
 load();
 setTitle(0);
 viewTeamList(teams, 0, defaultTeams);
 return stopLoad();
}

function showMatchList() {
 setTitle(1);
 return viewMatchList(false, 0, defaultShow);
}

function viewMatchList(active, start, len) {
 load();
 loadMatches();
 setRight(getMatchList(active, start, len));
 return stopLoad();
}

function loadMatches() {
 matches = new Array();
 var i, td = request('ajax?op=matches'), num, row, teams, j, scores;
 td = td.split('\n');
 num = 0;
 for (i = 0; i < td.length; i++) {
  if (!td[i]) continue;
  row = td[i].split(',');
  if (row.length < 6 + tpa * 2) continue;
  teams = new Array();
  for (j = 0; j < tpa * 2; j++)
   teams[j] = row[6 + j];
  if (row.length >= 6 + tpa * 4) {
   scores = new Array();
   for (j = 0; j < tpa * 2; j++)
    scores[j] = row[6 + tpa * 2 + j];
  } else {
   scores = null;
  }
  matches[num] = new ScheduleItem(new Date(parseInt(row[0])), row[1], row[2], row[3], teams, scores,
   row[4], row[5]);
  num++;
 }
}

function appendHeaderRow(out) {
 out += '<th width="60">Time<\/th><th width="100">#<\/th><th width="' + ((tpa + 1) * 50);
 out += '" colspan="' + (tpa + 1) + '">Red Alliance<\/th><th width="' + ((tpa + 1) * 50);
 out += '" colspan="' + (tpa + 1) + '">Blue Alliance<\/th><th width="90">Status<\/th>';
 return out;
}

function runLate() {
 load();
 setTitle(3);
 var out = '<form onsubmit="return doLate(this)" action="#">How many minutes is FIRST running late? ';
 out += '<input type="text" name="late" id="late" value="0" size="3" maxlength="3" value="' + late + '"/> <input ';
 out += 'type="submit" value="Run Late" /><\/form>';
 setRight(out);
 document.getElementById('late').focus();
 return stopLoad();
}

function doLate(form) {
 load();
 var val = parseInt(form.late.value);
 if (val == NaN) {
  alert('Please enter a number of minutes.');
  form.late.focus();
  return stopLoad();
 }
 request('ajax?op=late&index=' + val);
 setRight('<font color="#008000">Request successful.<\/font>');
 sTime();
 return stopLoad();
}

function enterMatch(ok) {
 load();
 setTitle(2);
 var out = '<form action="#" onsubmit="return matchEntry(this);"><table cellpadding="2" cellspacing="0" border="0">';
 out += '<tr><td align="right"><b>Time:<\/b><\/td><td><input type="text" id="time" name="time" size="5" maxlength="5"';
 if (lastTime)
  out += ' value="' + format(lastTime) + '"';
 out += ' /><\/td><td align="right"><b>When:<\/b><\/td><td><select name="when"><option value="0"';
 if (lastWhen == 0) out += ' selected="selected"';
 out += '>Today<\/option><option value="1"';
 if (lastWhen == 1) out += ' selected="selected"';
 out += '>Tomorrow<\/option>' + "\n";
 for (var i = 2; i < 5; i++) {
  out += '<option value="' + i + '"';
  if (lastWhen == i) out += ' selected="selected"';
  out += '>' + i + ' days ahead<\/option>';
 }
 out += '<\/select><\/td><\/tr><tr><td align="right"><b>Match #:<\/b><\/td><td><input type="text" name="num" size="3" ';
 out += 'maxlength="4" value="' + nextMatchNum + '" /><\/td><td align="right"><b>Label:</b><\/td><td><select name="label">';
 for (var i = 0; i < labels.length; i++) {
  out += '<option value="' + i + '"';
  if (lastLabel == i) out += ' selected="selected"';
  out += '>' + labels[i] + '<\/option>';
 }
 out += "\n" + '<\/select><\/td><\/tr><\/table><table cellpadding="2" cellspacing="0" border="0"><tr>';
 var o2 = '';
 for (var i = 0; i < tpa; i++) {
  out += '<td align="center" class="red">Red ' + (i + 1) + '<\/td>';
  o2 += '<td align="center" class="blue">Blue ' + (i + 1) + '<\/td>';
 }
 out += o2 + "\n";
 out += '<\/tr><tr>'
 o2 = '';
 for (var i = 0; i < tpa; i++) {
  out += '<td><input type="text" name="red' + (i + 1) + '" size="4" maxlength="5" /><\/td>';
  o2 += '<td><input type="text" name="blue' + (i + 1) + '" size="4" maxlength="5" /><\/td>';
 }
 out += o2 + "\n";
 out += '<\/tr><tr><td align="center" colspan="6"><input type="submit" value="Enter Match" /><\/td><\/tr><\/table><\/form>';
 if (ok)
  out += '<br /><br /><font color="#008000">Match entered successfully.<\/font>';
 setRight(out);
 document.getElementById('time').focus();
 return stopLoad();
}

function matchEntry(form) {
 load();
 var time = form.time.value;
 time = time.split(':');
 if (time.length < 2) {
  alert('Time is not in valid format.\nMust be in hh:mm (24 hour)');
  form.time.focus();
  return stopLoad();
 }
 var when = form.when.selectedIndex;
 if (when < 0 || when > 4) {
  alert('Please select a valid day.');
  form.when.focus();
  return stopLoad();
 }
 var now = getDate();
 now.setDate(now.getDate() + when);
 var hrs = parseInt(time[0]);
 var mins = parseInt(time[1]);
 if (hrs == NaN || mins == NaN || hrs <= 0 || mins < 0 || hrs > 24 || mins > 59) {
  alert('Time is not in valid format.');
  form.time.focus();
  return stopLoad();
 }
 now.setHours(hrs);
 now.setMinutes(mins);
 var team = new Array(tpa * 2), t;
 for (var i = 0; i < tpa; i++) {
  t = eval('form.red' + (i + 1) + '.value');
  team[2 * i] = parseInt(t);
  if (!team[2 * i] || !teams[team[2 * i]]) {
   alert('Team number "' + t + '" is not valid.');
   eval('form.red' + (i + 1) + '.focus()');
   return stopLoad();
  }
  t = eval('form.blue' + (i + 1) + '.value');
  team[2 * (i + tpa)] = parseInt(t);
  if (!team[2 * (i + tpa)] || !teams[team[2 * (i + tpa)]]) {
   alert('Team number "' + t + '" is not valid.');
   eval('form.blue' + (i + 1) + '.focus()');
   return stopLoad();
  }
  team[2 * i + 1] = 'f';
  team[2 * (i + tpa) + 1] = 'f';
 }
 var lbl = form.label.selectedIndex;
 if (lbl < 0 || lbl >= labels.length) {
  alert('Please select a valid match label.');
  form.label.focus();
  return stopLoad();
 }
 team[tpa * 4] = labels[lbl];
 t = parseInt(form.num.value);
 if (!t || t <= 0) {
  alert('Match number is not valid.');
  form.num.focus();
  return stopLoad();
 }
 nextMatchNum = t + 1;
 lastWhen = when;
 lastLabel = lbl;
 lastTime = now;
 lastTime.setMinutes(lastTime.getMinutes() + spacing);
 request('ajax?op=match&time=' + now.getTime() + '&data=' + escape(team.join(',')) + '&index=' + t);
 return enterMatch(true);
}

function request(data) {
 if (data.indexOf('?') < 0)
  data += '?sid=' + sid + '&ncr=' + Math.floor(Math.random() * 1E9);
 else
  data += '&sid=' + sid + '&ncr=' + Math.floor(Math.random() * 1E9);
 req.open('GET', data, false);
 req.send(null);
 if (req.status == 200)
  return req.responseText;
 alert('Request could not be sent to the server.\n\nTry again later, or refresh the page and try again.');
 return '';
}

var days = new Array('Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat');

function format(date) {
 var mins = date.getMinutes();
 if (mins < 10) mins = '0' + mins;
 return date.getHours() + ':' + mins;
}

function getMatchList(team, start, len) {
 var end = start + len, inc = len;
 var out = '';
 if (matches.length > 0) {
  out = '<table border="0" cellpadding="0" cellspacing="0" id="mlist">' + "\n";
  out = appendHeaderRow(out);
 } else {
  out += '<b>No Matches!<\/b><br />';
  return out;
 }
 var count = 0, match, i, bgColor, whoWon, whoWon2, hasMe;
 len = 0;
 for (matchNum in matches) {
  if (len < start || len >= end) {
   len++;
   continue;
  }
  hasMe = false;
  match = matches[matchNum];
  if (team) {
   for (i = 0; i < tpa * 2; i++)
    if (match.teams[i] == team) {
     hasMe = true;
     break;
    }
   if (!hasMe) continue;
  }
  hasMe = false;
  out += '<tr id="mr' + matchNum + '"><td class="mcell">' + format(match.time) + '<br />' + days[match.time.getDay()];
  out += '<\/td><td class="mcell"><font size="-1">';
  out += match.label + '<\/font><br />' + match.num + "<\/td>\n";
  whoWon = match.scoreOne > match.scoreTwo;
  whoWon2 = match.scoreOne < match.scoreTwo;
  bgColor = '#FF9999';
  var isComp = match.status.toLowerCase() == 'comp';
  for (i = 0; i < tpa * 2; i++)
   if (!team && match.teams[i] == myTeam) {
    hasMe = true; break;
   }
  if (hasMe) bgColor = '#FF0000';
  for (i = 0; i < tpa; i++) {
   out += '<td class="mcell" width="50" bgcolor="' + bgColor + '"><a href="#" id="ml_' + matchNum + '_' + i + '" onmouseover';
   out += '="return setStatus(\'Team ' + match.teams[i] + '\', \'ml_' + matchNum + '_' + i + '\');" onmouseout="return ';
   out += 'clrStatus(\'ml_' + matchNum + '_' + i + '\');" onclick="return showTeam(' + match.teams[i] + ')">';
   out += (whoWon ? '<b>' : '') + match.teams[i] + '<\/a>';
   out += (whoWon ? '<\/b>' : '');
   if (isComp && advScore)
    out += '<br /><font color="#FFFFFF">' + match.scores[i] + '<\/font>';
   out += '<\/td>';
   ie7fix('ml_' + matchNum + '_' + i);
  }
  out += '<td class="mcell" width="50" bgcolor="' + bgColor + '">' + (whoWon ? '<b>' : '') + 'Red<br />' + match.scoreOne;
  out += (whoWon ? '<\/b>' : '') + "<\/td>\n";
  bgColor = '#99BBFF';
  if (hasMe) bgColor = '#6699FF';
  for (i = tpa; i < tpa * 2; i++) {
   out += '<td class="mcell" width="50" bgcolor="' + bgColor + '"><a href="#" id="ml_' + matchNum + '_' + i + '" onmouseover';
   out += '="return setStatus(\'Team ' + match.teams[i] + '\', \'ml_' + matchNum + '_' + i + '\');" onmouseout="return ';
   out += 'clrStatus(\'ml_' + matchNum + '_' + i + '\');" onclick="return showTeam(' + match.teams[i] + ')">';
   out += (whoWon2 ? '<b>' : '') + match.teams[i] + '<\/a>';
   out += (whoWon2 ? '<\/b>' : '');
   if (isComp && advScore)
    out += '<br /><font color="#FFFFFF">' + match.scores[i] + '<\/font>';
   out += '<\/td>';
   ie7fix('ml_' + matchNum + '_' + i);
  }
  out += '<td class="mcell" width="50" bgcolor="' + bgColor + '">' + (whoWon2 ? '<b>' : '') + 'Blue<br />' + match.scoreTwo;
  out += (whoWon2 ? '<\/b>' : '') + '<\/td><td class="mcell" width="90"><b>';
  if (isComp)
   out += 'Completed';
  else
   out += 'Scheduled';
  out += '<br /><\/b><a id="ar_' + matchNum + '" target="_blank" href="/report?time=' + match.time.getTime();
  out += '" onmouseover="return setStatus(\'View Report\', \'ar_' + matchNum + '\');" onmouseout="return clrStatus(\'ar_';
  out += matchNum + '\');">report<\/a><\/td><\/tr>' + "\n";
  count++; len++;
 }
 var o2;
 if (len / inc >= 1)
  o2 = "Go To Page:\n";
 else
  o2 = "\n";
 active = team;
 for (i = 0; i < start; i += inc) {
  o2 += '<a href="#" onclick="return viewMatchList(active, ' + i + ', ' + inc + ')" onmouseover="return setStatus(\'Go To';
  o2 += ' Page\', \'goto_' + i + '\')" onmouseout="return clrStatus(\'goto_' + i + '\')" id="goto_' + i + '">';
  o2 += (i / inc + 1) + "<\/a>\n";
  ie7fix('goto_' + i);
 }
 if (len / inc >= 1) o2 += '<b>' + (i / inc + 1) + "<\/b>\n";
 i += inc;
 for (; i < len; i += inc) {
  o2 += '<a href="#" onclick="return viewMatchList(active, ' + i + ', ' + inc + ')" onmouseover="return setStatus(\'Go To';
  o2 += ' Page\', \'goto_' + i + '\')" onmouseout="return clrStatus(\'goto_' + i + '\')" id="goto_' + i + '">';
  o2 += (i / inc + 1) + "<\/a>\n";
  ie7fix('goto_' + i);
 }
 i = Math.min(end, len);
 if (start + 1 == i)
  out = 'Showing <b>' + (start + 1) + '<\/b> of <b>' + len + '<\/b> match' + (len == 1 ? '' : 'es') + '.<br />' + o2 + out;
 else
  out = 'Showing <b>' + (start + 1) + '-' + i + '<\/b> of <b>' + len + '<\/b> matches.<br />' + o2 + out;
 out += '<\/table>' + o2;
 return out;
}

function Team(number, name, rating, points, teamPoints, wins, losses, ties, type, udfs) {
 this.number = parseInt(number);
 this.name = name;
 this.rating = parseFloat(rating);
 this.points = parseInt(points);
 this.teamPoints = parseInt(teamPoints);
 this.wins = parseInt(wins);
 this.losses = parseInt(losses);
 this.ties = parseInt(ties);
 this.games = this.wins + this.losses + this.ties;
 this.type = type;
 this.udfs = udfs;
}

function ScheduleItem(time, num, label, status, teams, scores, scoreOne, scoreTwo) {
 this.time = time;
 this.num = num;
 this.label = label;
 this.status = status;
 this.teams = teams;
 this.scores = scores;
 this.scoreOne = scoreOne;
 this.scoreTwo = scoreTwo;
}

var tree = new Array();
tree[0] = 'teams|showTeamList()';
tree[1] = 'matches|showMatchList()';
tree[2] = 'enter match|enterMatch(false)';
tree[3] = 'late?|runLate()';
tree[4] = 'favorites|showFavList()';

var favTeams = new Array();
favTeams[0] = myTeam;

var teams = new Array();
var matches, active;

function setStatus(status, id) {
 window.status = status;
 document.getElementById(id).style.color = '#345373';
 return true;
}

function clrStatus(id) {
 window.status = ' ';
 document.getElementById(id).style.color = '#0000FF';
 return true;
}

function setTHL(num) {
 document.getElementById('ttd' + num).style.backgroundColor = '#7AA1E6';
 return setStatus('Team ' + num, 'teamlist_' + num);
}

function clearTHL(num) {
 document.getElementById('ttd' + num).style.backgroundColor = '#D6DFF7';
 return clrStatus('teamlist_' + num);
}

function sTime() {
 td = request('ajax?op=stime').split('\n');
 if (td.length > 1) {
  offset = parseInt(td[0]) - new Date().getTime();
  late = parseInt(td[1]);
 }
}

function writeTree() {
 if (!validBrowser) {
  alert("Your browser does not support the web interface.\nValid browsers include:\n+ Internet Explorer 6 or above\n" +
   "+ Mozilla Firefox\n+ Safari\n+Opera 8.0 or above");
  return false;
 }
 load();
 var i, td = request('ajax?op=name'), num, row, udf, j;
 td = td.split('\n');
 for (i = 0; i < td.length; i++) {
  if (!td[i]) continue;
  row = td[i].split(',');
  if (row.length < 9 + udfs.length) continue;
  udf = new Array();
  for (j = 0; j < udfs.length; j++)
   udf[j] = row[9 + j];
  num = row[0];
  teams[num] = new Team(num, row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], udf);
 }
 sTime();
 document.getElementById('welcome').innerHTML = realName + ', Team ' + myTeam +
  ' &nbsp; <a id="out" href="logout?sid=' + sid + '">log out<\/a>';
 var node;
 for (i = 0; i < tree.length; i++) {
  node = tree[i].split('|');
  document.write("<td class=\"mtd\" id=\"mtd" + i + "\" align=\"center\"><a id=\"tree_" + i + "\"");
  document.write(" onclick=\"return " + node[1] + ";\" onmouseover=\"return setStatus('" + node[0] + "', 'tree_");
  document.write(i + "');\" href=\"#\" onmouseout=\"return clrStatus('tree_" + i + "');\">" + node[0]);
  document.write("<\/a><\/td>\n");
  ie7fix('tree_' + i);
 }
 window.setTimeout('setTime()', 50);
 ie7done();
 return false;
}

function showImg(num) {
 var out = '';
 out += '<a href="#" onclick="return showTeam(' + num + ');" onmouseover="return setStatus(\'Go Back\', \'iback\');"';
 out += ' onmouseout="return clrStatus(\'iback\');" id="iback">Return to viewing ' + num + ".<\/a><br />\n";
 out += '<img border="0" src="images/teams/' + num + '.jpg" height="600" width="800" alt="[No Image]" />';
 ie7fix('iback');
 setRight(out);
 ie7done();
 return false;
}

function showTeam(num) {
 load();
 var team = teams[num];
 setTitle(-1);
 if (!team) {
  ie7fix('tl');
  setRight('Team <b>' + num + '<\/b> does not exist in this event.<br /><br /><a href="#" id="tl" onclick="return ' +
   'showTeamList();" onmouseover="return setStatus(\'Team List\', \'tl\')" onmouseout="return clrStatus(\'tl\')">' +
   "Return to team list<\/a>\n");
  return stopLoad();
 }
 var i, td = request('ajax?op=name&team=' + num), nm, row, udf, j, myUDFs;
 td = td.split('\n');
 for (i = 0; i < td.length; i++) {
  row = td[i].split(',');
  if (row.length < 9 + udfs.length) continue;
  nm = row[0];
  udf = new Array();
  for (j = 0; j < udfs.length; j++)
   udf[j] = row[9 + j];
  teams[nm] = new Team(nm, row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], udf);
  break;
 }
 myUDFs = udf;
 td = request('ajax?op=comments&team=' + num).split('\n');
 var comments = new Array(), users = new Array(), ratings = new Array(), cUdfs = new Array();
 nm = 0; var item, index = -1;
 for (i = 0; i < td.length; i++) {
  if (!td[i] || td[i].length < 2) continue;
  item = td[i].split(',');
  if (item.length < 4 + myUDFs.length) continue;
  if (item[0].toLowerCase() == name.toLowerCase()) index = nm;
  users[nm] = unescape(item[1]);
  ratings[nm] = parseInt(unescape(item[2]));
  udf = new Array();
  for (var j = 0; j < myUDFs.length; j++)
   udf[j] = parseInt(unescape(item[3 + j]));
  cUdfs[nm] = udf;
  comments[nm++] = unescape(item[item.length - 1]);
 }
 var tName = team.name;
 if (!tName) name = 'No Name';
 var out = '<a href="#" id="tl1" onclick="return showTeamList();" onmouseover="return setStatus(\'Team List\', \'tl1\')"' +
  'onmouseout="return clrStatus(\'tl1\')">Return to team list<\/a><br />';
 ie7fix('tl1');
 out += '<a href="#" id="img" onclick="return showImg(' + num + ');" onmouseover="return setStatus(\'View Image\', \'img\')"';
 out += ' onmouseout="return clrStatus(\'img\')"><img src="thumbnail?team=' + num + '" border="0" ';
 out += 'alt="[No Image]" align="left" id="i2" /><\/a><h1>Team ' + num + ': ' + tName + "<\/h1>\n";
 ie7fix('img');
 out += '<a href="#" id="ft" onclick="return setFav(' + num + ');" onmouseover="return ';
 out += 'setStatus(\'Toggle Favorite\', \'ft\');" onmouseout="return clrStatus(\'ft\');">&nbsp;<\/a>' + "\n";
 ie7fix('ft');
 out += '<table border="0" cellpadding="2" cellspacing="0"><tr><td align="right"><b>Record:<\/b><\/td><td>';
 out += team.wins + '-' + team.losses + '-' + team.ties + '<\/td><td align="right"><b>Percentage:<\/b><\/td><td>';
 if (team.games > 0)
  out += Math.round((team.wins + 0.5 * team.ties) * 100 / team.games) + '%';
 else
  out += '0%';
 out += '<\/td><\/tr><tr>';
 if (advScore) {
  out += '<td><b>Points/Game:<\/b><\/td><td>';
  if (team.games > 0)
   out += Math.round(team.points * 10 / team.games) / 10;
  else
   out += '0';
  out += '<\/td>';
 } else
  out += '<td>&nbsp;<\/td>';
 out += '<td><b>Alliance Points/Game:<\/b><\/td><td>';
 if (team.games > 0)
  out += Math.round(team.teamPoints * 10 / team.games) / 10;
 else
  out += '0';
 out += "<\/td><\/tr><\/table>\n<b>Rating:</b> &nbsp;";
 out += genRate(team.rating);
 out += "\n" + ' &nbsp; <b>Type:<\/b> &nbsp;<form onsubmit="return setType(' + num + ', this.type.selectedIndex);" ';
 out += 'id="comment" name="comment" action="#"><select onchange="return !setType(' + num + ', this.selectedIndex)">';
 var had = false;
 for (i = 0; i < types.length; i++) {
  out += '<option value="' + i + '"';
  if (types[i].toLowerCase() == team.type.toLowerCase()) {
   out += ' selected="selected"';
   had = true;
  } else if (!had && i >= types.length - 1)
   out += ' selected="selected"';
  out += '>' + types[i] + '<\/option>' + "\n";
 }
 out += '<\/select><\/form><br clear="all" />' + "\n";
 for (i = 0; i < comments.length; i++) {
  out += '<hr /><b>' + users[i] + '<\/b>: ' + genRate(ratings[i]) + '<br />' + htmlspecial(unescape(comments[i])) + '<br />';
  out += '<table cellpadding="1" cellspacing="0" border="0">';
  udf = cUdfs[i];
  for (var j = 0; j < udf.length; j++)
   out += '<tr><td align="right">' + udfs[j] + ': <\/td><td>' + udf[j] + '<\/td><\/tr>';
  out += "<\/table><br />\n";
 }
 out += '<hr /><b>Add/Edit Your Comments:<\/b><br /><b>Rating:</b> &nbsp;' + "\n";
 rate = 0;
 if (index >= 0) rate = ratings[index];
 for (i = 0; i < rate; i++) {
  out += '<a href="#" id="st' + i + '" onmouseover="return setStatus(\'Rate ' + (i + 1) + '\', \'st' + i + '\');" ';
  out += 'onmouseout="return clrStatus(\'st' + i + '\');" onclick="return doRate(' + num + ',' + (i + 1) + ');">';
  out += '<img src="imgrs?star-lit.png" border="0" alt="*" title="Star" height="16" width="16" /><\/a>';
  ie7fix('st' + i);
 }
 for (; i < 5; i++) {
  out += '<a href="#" id="st' + i + '" onmouseover="return setStatus(\'Rate ' + (i + 1) + '\', \'st' + i + '\');" ';
  out += 'onmouseout="return clrStatus(\'st' + i + '\');" onclick="return doRate(' + num + ',' + (i + 1) + ');">';
  out += '<img src="imgrs?star-unlit.png" border="0" alt=" " title="No Star" height="16" width="16" /><\/a>';
  ie7fix('st' + i);
 }
 out += "\n" + '<br /><table cellpadding="1" cellspacing="0" border="0">';
 tUdfs = new Array();
 if (index >= 0) tUdfs = cUdfs[index];
 else for (i = 0; i < myUDFs.length; i++)
  tUdfs[i] = 0;
 for (i = 0; i < tUdfs.length; i++) {
  out += '<tr><td align="right">' + udfs[i] + ':<\/td><td><form action="#" onsubmit="return false;">';
  out += '<input type="text" name="udfv" size="3" maxlength="8" value="' + tUdfs[i] + '" onchange="return !udf(';
  out += num + ', ' + i + ', this.form);" /><\/form><\/td><\/tr>' + "\n";
 }
 comment = '';
 if (index >= 0) comment = comments[index];
 out += '<\/table><span id="status">&nbsp;<\/span>';
 out += '<hr /><b>Comment: </b><form action="#" onsubmit="return setComment(' + num + ', this.com.value)"><br />';
 out += '<textarea name="com" id="com" rows="5" cols="40" wrap="virtual">' + htmlspecial(unescape(comment));
 out += '</textarea> <input type="submit" value="Update" /></form><hr /><b>Matches Involving This Team:</b><br />';
 loadMatches();
 out += getMatchList(num, 0, 99999);
 out += '<a onclick="return showTeamList();" onmouseover="return setStatus(\'Team List\', \'tl2\')"';
 out += 'onmouseout="return clrStatus(\'tl2\')" href="#" id="tl2">Return to team list<\/a>';
 ie7fix('tl2');
 setRight(out);
 favLink(favIndex(num) < 0);
 document.getElementById('com').focus();
 return stopLoad();
}

function htmlspecial(toEscape) {
 var nt = '', c;
 for (var i = 0; i < toEscape.length; i++) {
  c = toEscape.charAt(i);
  switch (c) {
   case '&':
    nt += '&amp;';
    break;
   case '<':
    nt += '&lt;';
    break;
   case '>':
    nt += '&gt;';
    break;
   default:
    nt += c;
  }
 }
 return nt;
}

function genRate(rate) {
 out = '';
 for (i = 0; i < rate; i++)
  out += '<img src="imgrs?star-lit.png" border="0" alt="*" title="Star" height="16" width="16" /><\/a>';
 var diff = rate - Math.floor(rate);
 if (diff < 0.125 && i < 5) {
  out += '<img src="imgrs?star-unlit.png" border="0" alt=" " title="No Star" height="16" width="16" /><\/a>';
  i++;
 } else if (diff >= 0.125 && diff < 0.375 && i < 5) {
  out += '<img src="imgrs?star-25.png" border="0" alt=" " title="25%" height="16" width="16" /><\/a>';
  i++;
 } else if (diff >= 0.375 && diff < 0.625 && i < 5) {
  out += '<img src="imgrs?star-50.png" border="0" alt="-" title="50%" height="16" width="16" /><\/a>';
  i++;
 } else if (diff >= 0.625 && diff < 0.875 && i < 5) {
  out += '<img src="imgrs?star-75.png" border="0" alt="*" title="75%" height="16" width="16" /><\/a>';
  i++;
 } else if (diff >= 0.875 && i < 5) {
  out += '<img src="imgrs?star-lit.png" border="0" alt="*" title="Star" height="16" width="16" /><\/a>';
  i++;
 }
 for (; i < 5; i++)
  out += '<img src="imgrs?star-unlit.png" border="0" alt=" " title="No Star" height="16" width="16" /><\/a>';
 return out;
}

function udf(num, index, form) {
 if (tUdfs.length < 1) return false;
 load();
 var val = parseInt(form.udfv.value);
 if (!val && val != 0) {
  alert('UDF value must be an integer.');
  form.udfv.focus();
  return stopLoad();
 }
 tUdfs[index] = parseInt(form.udfv.value);
 document.getElementById('status').innerHTML = 'Saving UDF...';
 window.setTimeout('clrUDF()', 1000);
 return updateComment(num);
}

function clrUDF() {
 document.getElementById('status').innerHTML = '&nbsp;';
 return true;
}

function doRate(num, rating) {
 load();
 rate = rating;
 return updateComment(num);
}

function setType(num, type) {
 load();
 request('ajax?op=type&team=' + num + '&data=' + escape(types[type]));
 teams[num].type = types[type];
 return stopLoad();
}

function setComment(num, comm) {
 if (tUdfs.length < 1) return false;
 comment = comm;
 return updateComment(num);
}

function updateComment(num) {
 if (tUdfs.length < 1) return false;
 load();
 if (comment.length < 1)
  request('ajax?op=comm&team=' + num + '&data=' + escape(rate + ',%20,' + tUdfs.join(',')));
 else
  request('ajax?op=comm&team=' + num + '&data=' + escape(rate + ',' + escape(comment) + ',' + tUdfs.join(',')));
 return showTeamEventually(num);
}

function showTeamEventually(num) {
 window.setTimeout('showTeam(' + num + ')', 300);
 return false;
}

function setFav(num) {
 var fav = favIndex(num);
 if (fav < 0) {
  var fv = favIndex(0);
  if (fv >= 0)
   favTeams[fv] = num;
  else
   favTeams[favTeams.length] = num;
 } else
  favTeams[fav] = 0;
 favLink(fav >= 0);
 return false;
}

function favLink(index) {
 if (index)
  document.getElementById('ft').innerHTML = 'Add to favorites';
 else
  document.getElementById('ft').innerHTML = 'Remove from favorites';
}

function favIndex(num) {
 for (var i = 0; i < favTeams.length; i++)
  if (favTeams[i] == num) return i;
 return -1;
}

function setRight(str) {
 document.getElementById('myContent').innerHTML = str;
 if (isIE7) window.setTimeout('ie7done()', 3);
 return false;
}

function setTitle(i) {
 if (old >= 0)
  document.getElementById('mtd' + old).style.backgroundColor = '#FFFFFF';
 old = i;
 if (i >= 0)
  document.getElementById('mtd' + i).style.backgroundColor = '#D6DFF7';
 return false;
}

preload();