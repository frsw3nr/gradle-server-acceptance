package Getconfig::GUI::ControlPanel;
use 5.008001;
use strict;
use warnings;
use Win32::GUI();

our $VERSION = "0.01";

sub new {
    my $class = shift;
    bless {
        command    => undef,
        @_,
    }, $class;
}

sub show {
    my $mw = Win32::GUI::Window->new(
        -name  => "mw", -title => "Getconfig",
        -pos   => [ 100, 100 ], -size  => [ 300, 350 ],
    );

    # メイン画面ボタン
    $mw->AddButton(-name => "Open",         -text => "Open",       -pos  => [ 10, 10 ],  );
    $mw->AddButton(-name => "AddTarget",    -text => "Add Target", -pos  => [ 60, 10 ],  );
    $mw->AddButton(-name => "ImportTarget", -text => "Import",     -pos  => [ 140, 10 ], );
    $mw->AddButton(-name => "Run",          -text => "Run",        -pos  => [ 200, 10 ], );

    $mw->AddTextfield(
        -name     => "TargetKeyword",
        -prompt   => [ "Keyword", 60 ],
        -pos  => [10, 40],
        -size => [100, 20],
    );

    $mw->AddCheckbox(-name => "Diff", -text => "Diff", -pos  => [200, 40],);

    my $lv = $mw->AddListView(
        # -imagelist        => $ilLarge,
        -pos              => [ 10, 70 ],
        -width            => $mw->ScaleWidth()-20,
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

    $mw->AddCombobox(
        -name => 'operation',
        -dropdownlist => 1,
        -pos  => [ 10, 280 ],
        -size => [ 60, 100 ],
    );
    $mw->operation->Add('Operation', 'Delete', 'Edit');
    $mw->operation->Select(0);

    ######################################################################
    # Add items to the listview
    # Currently undocumented that -text item can take an array ref
    # containing text for multiple columns
    for my $i (1..5) {
        $lv->InsertItem( -image => 0, -text  => ["Host".$i, "0.0.0.0", 'Linux,VM'],);
    }

    my $W2 = Win32::GUI::Window->new(
        -name  => "W2",
        -title => "Edit target",
        -pos   => [ 150, 150 ],
        -size  => [ 300, 400 ],
        -parent => $mw,
    );

    $W2->AddTextfield( -name => "Hostname", -prompt => [ "Hostname", 80 ], -pos => [10, 10], -size => [100, 20],);
    $W2->AddTextfield( -name => "IP",       -prompt => [ "IP",       80 ], -pos => [10, 40], -size => [100, 20],);


    $W2->AddButton(
        -name => "Button2",
        -text => "Close this window",
        -pos  => [ 10, 300 ],
    );

    $mw->Show();
    Win32::GUI::Dialog();
}

sub run {
    print "hello\n";
}

1;
__END__