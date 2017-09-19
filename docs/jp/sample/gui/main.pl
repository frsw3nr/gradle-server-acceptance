#!perl -w
use strict;
use warnings;

use Win32::GUI();

my $W1 = Win32::GUI::Window->new(
	-name  => "W1",
	-title => "First Window",
	-pos   => [ 100, 100 ],
	-size  => [ 300, 300 ],
);

$W1->AddButton(
	-name => "Open",
	-text => "Open",
	-pos  => [ 10, 10 ],
);

$W1->AddButton(
	-name => "AddTarget",
	-text => "Add Target",
	-pos  => [ 60, 10 ],
);

$W1->AddButton(
	-name => "ImportTarget",
	-text => "Import",
	-pos  => [ 140, 10 ],
);


$W1->AddButton(
	-name => "Run",
	-text => "Run",
	-pos  => [ 200, 10 ],
);

my $lv = $W1->AddListView(
	# -imagelist        => $ilLarge,
	-pos              => [ 10, 50 ],
	-width            => $W1->ScaleWidth()-20,
	-height           => 200,
	-editlabel        => 1,
	# -onBeginDrag      => \&beginDrag,
	# -onBeginLabelEdit => sub { return 1;},
	# -onEndLabelEdit   => sub { $_[0]->SetItemText($_[1],$_[2]) if defined $_[2] ; return 1; },
	# -onMouseMove      => \&drag,
	# -onMouseUp        => \&endDrag,
	# -onMouseDown      => \&preDrag,
);

# Add columns for the 'report' view
$lv->InsertColumn( -text => "Name",     -width => 50);
$lv->InsertColumn( -text => "IP",       -width => 50);
$lv->InsertColumn( -text => "Platform", -width => 100);

######################################################################
# Add items to the listview
# Currently undocumented that -text item can take an array ref
# containing text for multiple columns
for my $i (1..5) {
	$lv->InsertItem( -image => 0, -text  => ["Host".$i, "0.0.0.0", 'Linux,VM'],);
}

my $W2 = Win32::GUI::Window->new(
	-name  => "W2",
	-title => "Second Window",
	-pos   => [ 150, 150 ],
	-size  => [ 300, 200 ],
	-parent => $W1,
);

$W2->AddButton(
	-name => "Button2",
	-text => "Close this window",
	-pos  => [ 10, 10 ],
);

$W1->Show();
Win32::GUI::Dialog();
exit(0);

sub W1_Terminate {
	return -1;
}

sub Button1_Click {
	$W2->DoModal();
	return 0;
}

sub W2_Terminate {
	return -1;
}

sub Button2_Click {
	return -1;
}
