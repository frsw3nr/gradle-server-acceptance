#!/usr/bin/perl
#
# Getconfig GUI 
#

use strict;
use FindBin;
BEGIN { push(@INC, $FindBin::Bin . '/lib'); }
use Getconfig::GUI::ControlPanel;

eval {
    my $gui = new Getconfig::GUI::ControlPanel();
    $gui->show();
};
if ($@) {
  print "Error!\n$@";
  exit 1;
}
