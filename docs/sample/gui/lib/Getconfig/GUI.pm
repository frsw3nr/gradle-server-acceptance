package Getconfig::GUI;
use 5.008001;
use strict;
use warnings;

our $VERSION = "0.01";

sub new {
    my $class = shift;
    bless {
        command    => undef,
        @_,
    }, $class;
}

sub run {
    print "hello\n";
    my $panel = new ControlPanel();
    $panel->show();
}

1;
__END__

=encoding utf-8

=head1 NAME

Getconfig::GUI - It's new $module

=head1 SYNOPSIS

    use Getconfig::GUI;

=head1 DESCRIPTION

Getconfig::GUI is ...

=head1 LICENSE

Copyright (C) frsw3nr.

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

=head1 AUTHOR

frsw3nr E<lt>frsw3nr@gmail.comE<gt>

=cut

