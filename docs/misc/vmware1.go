package main

import (
        "context"
        "flag"
        "fmt"
        "net/url"
        "os"

        "github.com/vmware/govmomi"
		"github.com/vmware/govmomi/find"
	)

var ip = os.Getenv("TEST_IP")
var user = os.Getenv("TEST_OS_USER")
var pass = os.Getenv("TEST_OS_PASSWORD")

var envURL = fmt.Sprintf("https://%s/sdk", ip)
var urlDescription = fmt.Sprintf("ESX or vCenter URL [%s]", envURL)
var urlFlag = flag.String("url", envURL, urlDescription)

var envInsecure = true
var insecureDescription = fmt.Sprintf("Don't verify the server's certificate chain [%s]", envInsecure)
var insecureFlag = flag.Bool("insecure", envInsecure, insecureDescription)

func main() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	flag.Parse()
	u, err := url.Parse(*urlFlag)
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
	u.User = url.UserPassword(user, pass)
	c, err := govmomi.NewClient(ctx, u, *insecureFlag)
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
	// vSphereクライアントバージョン
	fmt.Println(c.Client.Client.Version)

	// データセンターの情報を取得する
	f := find.NewFinder(c.Client, true)
	dc, err := f.DefaultDatacenter(ctx)
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
	f.SetDatacenter(dc)
	fmt.Println(dc)
}
