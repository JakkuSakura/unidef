import setuptools

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()
extra_requirements = ["quickfix"]
with open("requirements.txt", "r", encoding="utf-8") as fh:
    requirements = []
    for line in fh.readlines():
        line = line.strip()
        req = line.split("#")[0].strip()
        if req and req not in extra_requirements:
            requirements.append(req)
setuptools.setup(
    name="unidef",
    version="0.2.1",
    author="Jiangkun QIU",
    author_email="qjk2001@gmail.com",
    description="Define once, run everywhere",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/qiujiangkun/unidef",
    project_urls={
        "Bug Tracker": "https://github.com/qiujiangkun/unidef/issues",
    },
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    packages=["unidef"],
    include_package_data=True,
    install_requires=requirements,
    extras_require={
        'full': extra_requirements
    },
    python_requires=">=3.6",
)
