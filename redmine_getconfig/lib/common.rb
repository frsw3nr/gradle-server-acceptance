module Common

  def wildcard(keyword = nil)
    (keyword.blank?) ? '%' : "%#{keyword}%"
  end

end
