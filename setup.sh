#!/bin/sh
pip_site=$(env python3 -m pip --version | awk '{print $4}')
site_packages="$(dirname "$pip_site")"
BASEDIR=$(dirname "$0")
unlink "$site_packages/unidef"
ln -s "$BASEDIR/unidef" "$site_packages/unidef"
