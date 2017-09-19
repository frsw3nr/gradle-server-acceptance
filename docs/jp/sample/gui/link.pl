  use strict;
  use warnings;

  use Win32::GUI 1.02;
  use Win32::GUI::HyperLink;

  # A window
  my $win = Win32::GUI::Window->new(
    -title => "HyperLink",
    -pos => [ 100, 100 ],
    -size => [ 240, 200 ],
  );

  # Simplest usage
  $win->AddHyperLink(
    -text => "http://www.perl.org/",
    -pos => [10,10],
  );

  $win->Show();
  Win32::GUI::Dialog();
  exit(0);
