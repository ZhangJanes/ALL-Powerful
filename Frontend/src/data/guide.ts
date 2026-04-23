export type GuideArticle = {
  slug: string
  title: string
  intro: string
  sections: { title: string; body: string; highlight?: boolean }[]
}

export const guideCategories: { key: string; title: string; count: number; desc: string }[] = [
  { key: 'insurance', title: '医保社保', count: 8, desc: '报销、转移、查询等' },
  { key: 'hospital', title: '看病就医', count: 6, desc: '异地就医、挂号、病历' },
  { key: 'idcard', title: '证件办理', count: 10, desc: '身份证、护照、户口本' },
  { key: 'other', title: '其他办事', count: 5, desc: '入学、公积金、违章等' },
]

const articles: Record<string, GuideArticle[]> = {
  insurance: [
    {
      slug: 'yibao-baoxiao',
      title: '医保报销',
      intro: '门诊/住院费用报销流程（示例内容）',
      sections: [
        { title: '一、办理条件', body: '参保状态正常，费用在报销目录内，票据齐全。' },
        {
          title: '二、所需材料',
          body: '医保卡、出院小结、费用清单、发票原件等。',
          highlight: true,
        },
        { title: '三、办理流程', body: '1. 出院结算 → 2. 提交材料 → 3. 审核 → 4. 到账。' },
        { title: '四、办理地点', body: '参保地医保经办窗口或线上政务平台。' },
        { title: '五、办理时间', body: '工作日窗口时间；线上 7×24 提交。' },
        { title: '六、办理周期', body: '审核约 10–30 个工作日（各地不同）。' },
        { title: '七、收费标准', body: '一般不收取报销手续费。' },
        { title: '八、避坑技巧', body: '注意报销时限、材料复印件份数、银行卡信息一致。' },
      ],
    },
    {
      slug: 'shebao-zhuanyi',
      title: '社保转移',
      intro: '跨省市养老保险/医保转移',
      sections: [{ title: '概要', body: '线上申请、两地经办机构协同办理。' }],
    },
  ],
  hospital: [
    {
      slug: 'yidi-jiuyi',
      title: '异地就医备案',
      intro: '备案后可直接结算或回参保地报销',
      sections: [
        { title: '条件', body: '长期异地居住、工作或转诊等情形。' },
        { title: '材料', body: '身份证、居住证明/工作证明等。', highlight: true },
        { title: '流程', body: '国家医保服务平台 APP 或小程序提交备案。' },
      ],
    },
  ],
  idcard: [
    {
      slug: 'id-reissue',
      title: '身份证补办',
      intro: '丢失/损坏补领流程',
      sections: [
        { title: '材料', body: '户口本、照片回执等（以当地派出所要求为准）。', highlight: true },
        { title: '地点', body: '户籍所在地派出所或政务大厅。' },
        { title: '周期', body: '一般 15–40 个工作日领取新证。' },
      ],
    },
  ],
  other: [
    {
      slug: 'gjj-tiqu',
      title: '公积金提取',
      intro: '购房、租房、还贷等情形',
      sections: [{ title: '提示', body: '各地政策差异大，请以公积金中心为准。' }],
    },
  ],
}

export function listArticles(category: string): GuideArticle[] {
  return articles[category] ?? []
}

export function getArticle(category: string, slug: string): GuideArticle | undefined {
  return listArticles(category).find((a) => a.slug === slug)
}
