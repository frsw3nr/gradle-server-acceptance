package main

import (
	"fmt"
	"os"

	"github.com/masterzen/winrm"
)

func main() {
	ip := os.Getenv("TEST_IP")
	pass := os.Getenv("TEST_OS_PASSWORD")
	endpoint := winrm.NewEndpoint(ip, 5985, false, false, nil, nil, nil, 0)
	client, err := winrm.NewClient(endpoint, "Administrator", pass)
	if err != nil {
		panic(err)
	}

	_, err2 := client.RunWithInput("Get-WmiObject -Class Win32_ComputerSystem", os.Stdout, os.Stderr, os.Stdin)
	fmt.Println(os.Stdout)
	fmt.Println(os.Stderr)
	if err2 != nil {
		panic(err2)
	}
}
