#!/bin/bash
cd target/classes/com
tar zcf com.tar cxb
scp com.tar ali:/www/
rm -rf com.tar
