ALTER TABLE lecture_table
  MODIFY COLUMN `status`
    ENUM('draft','pending','reject','published','registering','ongoing','finished','cancelled')
    NOT NULL
    COMMENT '讲座状态：draft草稿，pending待审核，reject已驳回，published已发布，registering报名中，ongoing进行中，finished已结束，cancelled已取消';
